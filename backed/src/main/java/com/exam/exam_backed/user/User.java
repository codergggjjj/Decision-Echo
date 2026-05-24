package com.exam.exam_backed.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private Integer status;
    private String role;
    @TableField("create_time")
    private LocalDateTime createTime;

    public User() {
    }

    public User(Long id, String username, String passwordHash, String nickname, Integer status) {
        this(id, username, passwordHash, nickname, null, status, null);
    }

    public User(Long id, String username, String passwordHash, String nickname, String avatarUrl, Integer status) {
        this(id, username, passwordHash, nickname, avatarUrl, status, null);
    }

    public User(Long id, String username, String passwordHash, String nickname, String avatarUrl, Integer status, LocalDateTime createTime) {
        this(id, username, passwordHash, nickname, avatarUrl, status, "user", createTime);
    }

    public User(Long id, String username, String passwordHash, String nickname, String avatarUrl, Integer status, String role, LocalDateTime createTime) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.status = status;
        this.role = role == null ? "user" : role;
        this.createTime = createTime;
    }

    public Long id() {
        return id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String username() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String nickname() {
        return nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String avatarUrl() {
        return avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer status() {
        return status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String role() {
        return role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime createTime() {
        return createTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean enabled() {
        return Integer.valueOf(1).equals(status);
    }

    public boolean admin() {
        return "admin".equals(role);
    }
}
