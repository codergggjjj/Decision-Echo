# 登录功能测试数据

## 测试账号

| 场景 | username | password | 状态 | 预期 |
| --- | --- | --- | --- | --- |
| 正常用户 | test_user | Test@123456 | 启用 | 登录成功，返回 token |
| 管理员用户 | admin | Admin@123456 | 启用 | 登录成功，返回 token |
| 禁用用户 | disabled_user | Disabled@123456 | 禁用 | 登录失败，提示账号已禁用 |
| 错误密码 | test_user | Wrong@123456 | 启用 | 登录失败，提示账号或密码错误 |
| 不存在用户 | missing_user | Test@123456 | 不存在 | 登录失败，提示账号或密码错误 |

## 验证码测试

| 场景 | 输入 | 预期 |
| --- | --- | --- |
| 正确验证码 | 使用图片中显示的 4 位数字 | 继续校验账号密码 |
| 错误验证码 | 0000 | 返回验证码错误 |
| 空验证码 | 空字符串 | 返回参数错误 |
| 过期验证码 | 获取后等待超过 5 分钟再提交 | 返回验证码已过期 |
| 重复验证码 | 同一个 captchaId 提交两次 | 第二次返回验证码不存在或已失效 |

## 接口验证步骤

1. 请求 `GET /api/auth/captcha`，记录 `data.captchaId`，读取图片中的验证码。
2. 请求 `POST /api/auth/login`，请求体示例：

```json
{
  "username": "test_user",
  "password": "Test@123456",
  "captchaId": "接口返回的captchaId",
  "captchaCode": "图片中的4位数字"
}
```

3. 成功响应应包含 `data.token` 和 `data.user.username=test_user`。
4. 使用 `Authorization: Bearer <token>` 请求 `GET /api/auth/me`，应返回当前用户。
5. 请求 `POST /api/auth/logout` 后，再使用同一个 token 请求 `GET /api/auth/me`，应返回 401。

## SQL 数据位置

- 表结构：`backed/sql/schema.sql`
- 测试数据：`backed/sql/data.sql`
