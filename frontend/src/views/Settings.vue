<template>
  <div class="settings-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="user-card">
          <div class="user-header">
            <el-avatar :size="80" :src="userInfo.avatar" class="user-avatar">
              {{ userInfo.realName ? userInfo.realName.charAt(0) : 'U' }}
            </el-avatar>
            <h2 class="user-name">{{ userInfo.realName || userInfo.username }}</h2>
            <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'primary'" size="large" effect="dark">
              {{ userInfo.role === 'ADMIN' ? '管理员' : '读者' }}
            </el-tag>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.borrowedCount || 0 }}</div>
              <div class="stat-label">当前借阅</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.maxBorrowCount || 5 }}</div>
              <div class="stat-label">最大可借</div>
            </div>
          </div>
          <el-divider />
          <div class="user-detail">
            <div class="detail-item">
              <span class="detail-label">用户名</span>
              <span class="detail-value">{{ userInfo.username }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">账号状态</span>
              <el-tag :type="userInfo.status === 1 ? 'success' : 'info'" size="small">
                {{ userInfo.status === 1 ? '正常' : '已禁用' }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="detail-label">注册时间</span>
              <span class="detail-value">{{ userInfo.createTime ? userInfo.createTime.substring(0, 10) : '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">个人资料</span>
              <el-tag type="info" size="small">可编辑</el-tag>
            </div>
          </template>
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px" style="max-width: 500px">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled>
                <template #append>不可修改</template>
              </el-input>
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveProfile" :loading="saveLoading">保存修改</el-button>
              <el-button @click="resetProfileForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="form-card" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span class="card-title">修改密码</span>
              <el-tag type="warning" size="small">安全设置</el-tag>
            </div>
          </template>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" style="max-width: 500px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（6-20位）" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">确认修改</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const userInfo = ref({})
const saveLoading = ref(false)
const passwordLoading = ref(false)
const profileFormRef = ref()
const passwordFormRef = ref()

const profileForm = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadUserInfo = () => {
  request.get('/users/info').then(res => {
    userInfo.value = res.data
    profileForm.id = res.data.id
    profileForm.username = res.data.username
    profileForm.realName = res.data.realName
    profileForm.phone = res.data.phone
    profileForm.email = res.data.email
  }).catch(() => {})
}

const handleSaveProfile = () => {
  profileFormRef.value.validate((valid) => {
    if (valid) {
      saveLoading.value = true
      request.put('/users', profileForm).then(() => {
        ElMessage.success('个人资料修改成功')
        loadUserInfo()
        const cached = JSON.parse(localStorage.getItem('userInfo') || '{}')
        cached.realName = profileForm.realName
        localStorage.setItem('userInfo', JSON.stringify(cached))
      }).finally(() => {
        saveLoading.value = false
      })
    }
  })
}

const resetProfileForm = () => {
  profileForm.realName = userInfo.value.realName
  profileForm.phone = userInfo.value.phone
  profileForm.email = userInfo.value.email
}

const handleChangePassword = () => {
  passwordFormRef.value.validate((valid) => {
    if (valid) {
      passwordLoading.value = true
      request.put('/users/change-password', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      }).then(() => {
        ElMessage.success('密码修改成功，请重新登录')
        setTimeout(() => {
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
        }, 1500)
      }).finally(() => {
        passwordLoading.value = false
      })
    }
  })
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.user-card {
  position: sticky;
  top: 20px;
}
.user-header {
  text-align: center;
  padding: 20px 0;
}
.user-avatar {
  margin-bottom: 16px;
  font-size: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.user-name {
  margin: 0 0 12px;
  font-size: 22px;
  color: #303133;
}
.user-stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 16px 0;
  background: #f5f7fa;
  border-radius: 8px;
  margin: 0 10px;
}
.stat-item {
  text-align: center;
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.stat-divider {
  width: 1px;
  height: 40px;
  background: #e4e7ed;
}
.user-detail {
  padding: 0 10px;
}
.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.detail-item:last-child {
  border-bottom: none;
}
.detail-label {
  color: #909399;
  font-size: 14px;
}
.detail-value {
  color: #303133;
  font-size: 14px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
