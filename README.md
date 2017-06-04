# bookstoreAppDemo
打开Android Studio  
根据https://jingyan.baidu.com/article/eae07827b023af1fec5485a6.html 导入此项目  
  
## 注意事项  
1. 一开始会报错三个iml文件找不到，这个没关系。等到gradle build完就有了  
2. 第一次gradle build 会很慢，如果之前跑过android项目，可以不下载新的gradle，把版本改成现在有的  
（参考http://m.blog.csdn.net/article/details?id=48789705）
3. 如果遇到了某个包找不到的问题，这说明你很不走运，你的gradle没下载下来这个包。  
首先，尝试各种方法下载（按照错误提示install）  
不行的话可以在file->Project Structure里选择Modules，Dependicies去掉找不到的版本的包，在右上角+ ->Library Dependency换成自己有的包 或者新建一个空的项目，看看自带的包的版本是什么，更新gradle文件
