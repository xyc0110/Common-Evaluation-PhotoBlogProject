from django.shortcuts import render, get_object_or_404, redirect
from django.utils import timezone
from .models import Post
from .forms import PostForm

# ------------------- HTML 页面视图 -------------------

def post_list(request):
    posts = Post.objects.filter(published_date__lte=timezone.now()).order_by('published_date')
    return render(request, 'blog/post_list.html', {'posts': posts})

def post_detail(request, pk):
    post = get_object_or_404(Post, pk=pk)
    return render(request, 'blog/post_detail.html', {'post': post})

def post_new(request):
    if request.method == "POST":
        form = PostForm(request.POST, request.FILES)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm()
    return render(request, 'blog/post_edit.html', {'form': form})

def post_edit(request, pk):
    post = get_object_or_404(Post, pk=pk)
    if request.method == "POST":
        form = PostForm(request.POST, request.FILES, instance=post)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm(instance=post)
    return render(request, 'blog/post_edit.html', {'form': form})


# ------------------- Django REST Framework API -------------------

from rest_framework import viewsets, status
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from .serializers import PostSerializer
from django.contrib.auth.models import User


class BlogImages(viewsets.ModelViewSet):
    """
    REST API: 提供 Post 模型的 CRUD 操作。
    供 Android 端 /api_root/Post/ 调用。
    """
    serializer_class = PostSerializer
    permission_classes = [AllowAny]

    # ✅ 仅返回已发布的图片（过滤掉未成功上传的）
    def get_queryset(self):
        return Post.objects.filter(published_date__lte=timezone.now()).order_by('-published_date')

    # ✅ Android 上传时自动设置 author 和发布时间
    def perform_create(self, serializer):
        if self.request.user.is_authenticated:
            author = self.request.user
        else:
            author = User.objects.get(pk=1)  # 默认绑定第一个用户
        serializer.save(author=author, published_date=timezone.now())

    # ✅ 允许移动端 POST 时缺省字段（title/text 自动填充）
    def create(self, request, *args, **kwargs):
        data = request.data.copy()
        data.setdefault('title', '모바일 업로드 이미지')
        data.setdefault('text', '안드로이드 앱에서 업로드됨')
        data.setdefault('author', 1)

        serializer = self.get_serializer(data=data)
        if serializer.is_valid():
            self.perform_create(serializer)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        else:
            print("❌ Serializer Errors:", serializer.errors)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
