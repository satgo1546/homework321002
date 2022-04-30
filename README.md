# 趣味本地音视频播放库 #

*再见了，jvav*

@satgo1546：还剩两周左右的时间:dog:本来想整个QArt生成器啊，数字电路教学应用啊，更好用的浏览器啊，不过我在B站上看到一个[让人眼前一黑的好活](https://www.bilibili.com/video/av424928423)，我们整个安卓版也不是不行，还可以加个定时和结算假装做了个音游。

@Daniel-ChenJH：确实有意思。

@OvertheBrain：活有点太狠了。以这学期学的技术感觉做游戏有点难（？）可以沿用这个思路做一个音视频播放器。想了一个趣味音视频播放器：可以app里内置几个音乐/视频，用户可以往app里导入用特定格式编写的歌词本，软件将歌词展示成类似于视频里的样子，并将音视频根据歌词分块——就是分割歌词，像音乐软件里根据歌词播放。

@stevenzmc：这样的话我们是要根据歌词本生成不同的控件来展示歌词吗？

@satgo1546：“特定格式的歌词本”指XML layout（不是）。如果不想整活浓度过高的话就不要搞各种控件了，自定义一个歌词显示视图，自己绘制就是。

@OvertheBrain：主要layout真没啥控件可以用……原生开发有什么成熟的组件库吗？我之前开发app都是用React-Native的（

@satgo1546：确实，WinForm有现成的ListBox可以用，Android连回收视图转接器也得自己写。可能每种组件各有一些成熟的库，就用很长很长的依赖列表掩盖过去吧！

---

*两天后*

@OvertheBrain：但我在想做这么一个东西出来没啥屌用，而且学到的知识也没用几个，比如[他们给我们的API](https://bd-open-lesson.bytedance.com/#/apiList)不用感觉怪浪费的。还有就是可行性：每首歌的歌词都是不一样的，怎么区分，怎么播放。

@satgo1546：思考：没有屌用是否是其最大的屌用？要说的话多媒体、UI、动画，还是用得蛮足的。我个人倾向于不要用给的API……这会使有人三十年后把作业APK翻出来回忆（挖黑历史）时发现运行不了，原因是API已经没了。
关于带歌词音视频播放，ASS字幕和LRC歌词文件给出了标准解决方案：同目录下同名不同扩展名的文件即是对应歌词文件。又例如实现动画时，也可以参考ASS中的动画效果指令等。这么说来，创意性又没有了，不如还是做音游算了（x
其他两人有何想法？

@stevenzmc：搞个本地音频/视频库app？比较类似的我就想到这个。

---

*一周后*

@satgo1546：好耶，拖到只剩一周了！不管怎么样先把库建起来吧。……我都邀请了，被邀请者需要登上GitHub手动确认一下:cat: 鉴于除了趣味本地音视频播放库以外没有其他想法，就做这个吧。

@Daniel-ChenJH：可以。
