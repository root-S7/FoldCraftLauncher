<div align="center">
    <img width="75" src="/FCL/src/main/res/drawable/img_app.png"></img>
</div>

<h1 align="center">Fold Craft Launcher —— 直装版</h1>

<div align="center">

[![QQ](https://img.shields.io/badge/QQ-4903FC?style=for-the-badge&logoColor=white)](https://qm.qq.com/q/EHHDtWjdFS)
![Downloads](https://img.shields.io/github/downloads/FCL-Team/FoldCraftLauncher/total?color=green&style=for-the-badge)
[![Sponsor](https://img.shields.io/badge/sponsor-30363D?style=for-the-badge&logo=GitHub-Sponsors&logoColor=#EA4AAA)](https://afdian.com/@tungs)

</div>

- 该启动器属于第三方分支，主要用于制作『直装整合包』并分发给用户。

- 当用户下载好『制作后的APK』可直接一键安装游戏资源并启动游戏

<h1 align="center">新增功能（相较于官方分支）</h1>

- [x] 可自定义按键存放位置
- [x] 可自定义首次安装时皮肤站列表
- [x] 可自定义默认背景图和某些图片
- [x] 可自定义APK中运行环境打包项（详情见群文件说明）
- [x] 可自定义『config.json』和『menu_setting.json』文件配置项
- [x] 可在『general_setting.properties』文件中修改部分启动器设置选项
- [x] 打开应用后可自动检查配置文件格式是否正确
- [x] 以及更多内容！

<h1 align="center">截图</h1>

![GameScreen1](https://icraft.ren:90/tmp/FCL-image/1.jpg)
![GameScreen2](https://icraft.ren:90/tmp/FCL-image/2.jpg)
![GameScreen3](https://icraft.ren:90/tmp/FCL-image/3.jpg)

<h1 align="center">如何构建项目</h1>

建议加群下载最新版即可；如果你偏要自行构建，那么按照下面步骤做即可
1. 首先下载一个Android Studio并配置环境
2. 使用git命令克隆该项目（git clone ....）
   ![2_1](https://icraft.ren:90/tmp/FCL-image/2_1.jpg)
4. 将项目导入到Android Studio中
5. 修改项目根目录下的『local.properties』
6. 在『local.properties』文件中增加以下键值对
   * key-store-password
   * oauth-api-key
   * curse-api-key
     ![2_2](https://icraft.ren:90/tmp/FCL-image/2_2.jpg)
7. 在『Android Studio』右侧菜单栏找到『Gradle』
   ![2_3](https://icraft.ren:90/tmp/FCL-image/2_3.png)
   ![2_4](https://icraft.ren:90/tmp/FCL-image/2_4.png)
8. 在输入框输入『gradlew assemblerelease -Darch=arm64』并回车
   ![2_5](https://icraft.ren:90/tmp/FCL-image/2_5.png)
   ![2_6](https://icraft.ren:90/tmp/FCL-image/2_6.png)
   ![2_7](https://icraft.ren:90/tmp/FCL-image/2_7.png)
9. 在项目的『FCL/build/outputs/apk/release』下找到生成的APK文件
   ![2_8](https://icraft.ren:90/tmp/FCL-image/2_8.png)