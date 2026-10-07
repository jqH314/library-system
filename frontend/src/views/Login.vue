<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon class="logo-icon"><Reading /></el-icon>
        <h1>在线图书借阅管理系统</h1>
        <p>Library Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>
      <div class="login-tips">
        <p class="tips-title">测试账号（点击快速填充）：</p>
        <div class="account-list">
          <div class="account-item admin" @click="fillAccount('admin', 'admin123')">
            <span class="account-role">管理员</span>
            <span class="account-info">admin / admin123</span>
          </div>
          <div class="account-item reader" @click="fillAccount('2023001', '123456')">
            <span class="account-role">读者</span>
            <span class="account-info">2023001 / 123456</span>
          </div>
        </div>
        <p class="tips-note">管理员进入管理后台，读者进入个人中心</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import request from '@/api/request'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      request.post('/auth/login', form).then(res => {
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        ElMessage.success('登录成功')
        if (res.data.role === 'ADMIN') {
          router.push('/dashboard')
        } else {
          router.push('/browse')
        }
      }).finally(() => {
        loading.value = false
      })
    }
  })
}

const fillAccount = (username, password) => {
  form.username = username
  form.password = password
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.logo-icon {
  font-size: 48px;
  color: #667eea;
}
.login-header h1 {
  font-size: 22px;
  color: #303133;
  margin: 12px 0 6px;
}
.login-header p {
  font-size: 13px;
  color: #909399;
}
.login-btn {
  width: 100%;
  margin-top: 10px;
}
.login-tips {
  margin-top: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}
.tips-title {
  color: #606266;
  font-weight: 600;
  margin-bottom: 8px;
}
.account-list {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}
.account-item {
  flex: 1;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.account-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.account-item.admin {
  background: #fef0f0;
  border: 1px solid #fbc4c4;
}
.account-item.reader {
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
}
.account-role {
  font-weight: 600;
  font-size: 13px;
}
.account-item.admin .account-role {
  color: #f56c6c;
}
.account-item.reader .account-role {
  color: #409eff;
}
.account-info {
  font-size: 11px;
  color: #606266;
}
.tips-note {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}
</style>
