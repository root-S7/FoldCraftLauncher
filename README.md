<div align="center">
    <img width="75" src="/FCL/src/main/res/drawable/img_app.png"></img>
</div>

<h1 align="center">Fold Craft Launcher —— 整合包版</h1>

<div align="center">

[![QQ群](https://img.shields.io/badge/QQ-4903FC?style=for-the-badge&logoColor=white)](https://qm.qq.com/q/EHHDtWjdFS)
[![Downloads](https://img.shields.io/github/downloads/FCL-Team/FoldCraftLauncher/total?color=green&style=for-the-badge)]([https://qm.qq.com/q/EHHDtWjdFS](https://github.com/root-S7/FoldCraftLauncher/releases))
[![Sponsor](https://img.shields.io/badge/sponsor-30363D?style=for-the-badge&logo=GitHub-Sponsors&logoColor=#EA4AAA)](https://afdian.com/@tungs)

</div>

- 该启动器属于第三方分支，主要用于制作『直装整合包』并分发给用户。
- 当用户下载好『制作后的APK』可直接一键安装游戏资源并启动游戏

## ✨新增功能『相较于官方分支』

- [x] 可自定义按键存放位置
- [x] 可自定义默[RuntimeUtils.java](FCL/src/main/java/com/tungsten/fcl/util/RuntimeUtils.java)认背景图和某些图片
- [x] 打开应用后可自动检查配置文件格式是否正确
- [x] 可根据自己整合包情况删减无用Java运行环境
- [x] 可自定义APK中运行环境打包项（详情见群文件说明）
- [x] 可自定义『config.json』和『menu_setting.json』文件配置项
- [x] 可自定义首次安装时皮肤站列表『authlib_injector_server.json』
- [x] 可在『custom_renderer.json』内置特定渲染器（具体教程见群文件）
- [x] 可在『general_setting.properties』文件中修改部分启动器设置选项
- [x] 可在『launcher_rules.json』自定义特定版本启动规则（支持对渲染器，Java，运行内存作要求）
- [x] 以及更多内容！

## 🖼️截图

<div align="center">
  <img src="/.github/images/ui_main_light.jpg" width="30%" alt="浅色界面">
  <img src="/.github/images/ui_main_dark.jpg" width="30%" alt="深色界面">
  <img src="/.github/images/ui_not_install_runtime.jpg" width="30%" alt="资源安装">
</div>

## 🔨如何手动构建项目

建议加群下载最新版即可；如果你偏要自行构建，那么按照下面步骤做即可
1. 首先需要下载[Android Studio](https://developer.android.google.cn/studio)并安装
   <br><img src="/.github/images/download_android_studio.png" alt="安装Android Studio" width="444">
2. 然后下载[Gradle](https://gradle.org/releases)配置环境变量，并且需要在Android Studio中引入
3. 使用git命令克隆该项目（git clone ....）
   <br><img src="/.github/images/clone_project.jpg" alt="克隆项目" width="444">
4. 将项目导入到Android Studio中
5. 修改项目根目录下的『local.properties』
6. 在『local.properties』文件中增加以下键值对
   * key-store-password
   * oauth-api-key
   * curse-api-key
     <br><img src="/.github/images/edit_local_file.jpg" alt="编辑local.properties文件" width="333">
7. 在『Android Studio』右侧菜单栏找到『Gradle』
   <br><img src="/.github/images/find_gradle_icon.png" alt="找到Gradle选项" width="444">
   <br><img src="/.github/images/click_gradle_icon.png" alt="点击Gradle" width="444">
8. 在输入框输入『gradlew assemblerelease -Darch=arm64』并回车
   <br><img src="/.github/images/input_command.png" alt="输入命令" width="444">
   <br><img src="/.github/images/wait_run.png" alt="输入命令" width="444">
   <br><img src="/.github/images/run_result.png" alt="输入命令" width="444">
9. 在项目的『FCL/build/outputs/apk/release』下找到生成的APK文件
   <br><img src="/.github/images/find_build_apk.png" alt="输入命令" width="444">
