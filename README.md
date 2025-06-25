# CSword - 跨服传送插件

![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

一个基于cs插件制作的 Minecraft 跨服传送插件，支持经济扣费、免费模式、多服务器配置和热重载功能。

## 功能特性

- ✅ 多服务器传送支持
- 💰 首次传送扣费机制（支持 Vault 经济系统）
- 🆓 免费模式（可配置或自动检测）
- 🔄 热重载配置（无需重启服务器）
- ⏳ 传送倒计时提示
- 🔄 同服务器传送检测
- 📊 玩家付费状态持久化存储

## 安装指南

1. 将插件放入 `plugins/` 文件夹
2. 启动服务器生成配置文件
3. 修改 `plugins/CSword/config.yml`
4. 重启服务器（首次安装需要）

## 配置文件

```yaml
# config.yml 示例
free-mode: false //设置为true则为免费模式
current-server: "lobby" //填写当前服务器名称

servers:
  lobby:      //填写服务器名称
    cost: 0     //填写传送的价格
  survival:      //以此类推即可
    cost: 50
  skyblock:
    cost: 100
```

## 命令用法
| 命令        | 描述                          | 权限 |
|------------|------------------------------|--------|
| /cs   | 显示可用服务器列表       |无|
| /cs <服务器名>	   | 传送到指定服务器  |   无         |
| /cs reload     | 重载插件配置|csword.reload |

## 权限节点
|权限节点|	描述	|默认|
|------------|------------------------------|--------|
|csword.reload	|允许重载插件配置|	OP|

# 开发者指南
## 编译要求
```yaml
JDK 21+
Maven 3.8+
PaperMC API
```
## 构建步骤
```yaml
mvn clean package
//编译后的插件将生成在 target/CSword-版本号.jar
```
## 常见问题
### Q: 为什么传送不扣钱？
A: 1) 检查是否启用免费模式 2) 确保已安装 Vault 和经济插件
### Q: 如何设置当前服务器名称？
A: 在 config.yml 中修改 current-server 值
### Q: 重载命令无效？
A: 需要 csword.reload 权限，OP 默认拥有

## 更新日志
### v1.0
1.添加热重载功能
2.优化经济系统检测逻辑
3.修复同服务器传送问题

## 开源协议
本项目采用 MIT License
