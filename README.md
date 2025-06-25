# CSword - 跨服传送插件

![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

一个基于cs插件制作的 Minecraft 跨服传送插件，支持经济扣费、免费模式、多服务器配置和热重载功能。

## 功能特性
### 1. 跨服传送
- 通过 BungeeCord 通道将玩家传送到指定子服务器
### 2. 多服务器配置
- 支持在 `config.yml` 中配置多个目标服务器及独立价格
### 3. 经济系统集成
- 首次传送自动扣费（需 Vault + 经济插件），后续免费
### 4. 同服检测
- 自动阻止传送到当前所在服务器
- 提示 "您已在目标服务器！"
### 5. 免费模式
- 配置 `free-mode: true` 或未安装经济插件时，所有传送免费
### 6. 动态倒计时
- 传送前 3 秒倒计时提示（可配置时长）
### 7. 数据持久化
- 记录玩家付费状态到 `data.yml`
- 重启服务器后仍有效
### 8. 热重载配置
- 通过 `/cs reload` 实时重载配置
- 无需重启服务器
### 9. 智能扣费逻辑
- 仅首次传送扣费
- 余额不足时取消传送并提示
## 适用场景
- **多世界生存服**  
  主城/空岛/地牢等场景切换
- **小游戏群组服**  
  大厅自动扣费传送
- **测试服快速切换环境**  
  使用免费模式
  
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
