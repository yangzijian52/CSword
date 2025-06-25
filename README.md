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
