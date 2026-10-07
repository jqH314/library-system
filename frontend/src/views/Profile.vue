<template>
  <div class="profile-page">
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
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-value" :class="{ 'text-warning': overdueCount > 0 }">{{ overdueCount }}</div>
              <div class="stat-label">逾期数量</div>
            </div>
          </div>
          <el-divider />
          <div class="user-detail">
            <div class="detail-item">
              <span class="detail-label">用户名</span>
              <span class="detail-value">{{ userInfo.username }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">手机号</span>
              <span class="detail-value">{{ userInfo.phone || '未填写' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">邮箱</span>
              <span class="detail-value">{{ userInfo.email || '未填写' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">账号状态</span>
              <el-tag :type="userInfo.status === 1 ? 'success' : 'info'" size="small">
                {{ userInfo.status === 1 ? '正常' : '已禁用' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">我的借阅记录</span>
              <el-radio-group v-model="activeTab" size="default" @change="loadBorrowRecords">
                <el-radio-button value="">全部</el-radio-button>
                <el-radio-button value="BORROWED">借阅中</el-radio-button>
                <el-radio-button value="OVERDUE">已逾期</el-radio-button>
                <el-radio-button value="RETURNED">已归还</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-table :data="borrowRecords" stripe v-loading="borrowLoading" :empty-text="emptyText">
            <el-table-column prop="id" label="记录ID" width="90" />
            <el-table-column prop="bookId" label="图书ID" width="90" />
            <el-table-column prop="borrowTime" label="借阅时间" width="170" />
            <el-table-column prop="dueTime" label="应还时间" width="170" />
            <el-table-column prop="returnTime" label="归还时间" width="170">
              <template #default="{ row }">
                {{ row.returnTime || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                <div v-if="row.status === 'OVERDUE' && row.finePaid === 1" style="font-size: 11px; color: #67c23a; margin-top: 2px">罚款已缴</div>
                <div v-if="row.status === 'OVERDUE' && row.finePaid === 0" style="font-size: 11px; color: #f56c6c; margin-top: 2px">罚款未缴</div>
              </template>
            </el-table-column>
            <el-table-column prop="fineAmount" label="罚款" width="100" align="center">
              <template #default="{ row }">
                <span v-if="row.fineAmount > 0 && row.finePaid === 0" style="color: #f56c6c; font-weight: bold">¥{{ row.fineAmount }}</span>
                <span v-else-if="row.fineAmount > 0 && row.finePaid === 1" style="color: #67c23a">已缴</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 'BORROWED'" size="small" type="primary" @click="handleRenew(row)">续借</el-button>
                <el-button v-if="row.status === 'OVERDUE' && row.finePaid === 0" size="small" type="warning" @click="handlePayFine(row)">缴罚款</el-button>
                <span v-if="row.status === 'OVERDUE'" style="font-size: 11px; color: #909399; display: block; margin-top: 4px">请联系管理员归还</span>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pagination"
            background
            layout="total, prev, pager, next"
            :total="borrowTotal"
            :page-size="borrowPage.size"
            :current-page="borrowPage.page"
            @current-change="handleBorrowPageChange"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const userInfo = ref({})
const borrowRecords = ref([])
const borrowTotal = ref(0)
const borrowLoading = ref(false)
const activeTab = ref('')

const borrowPage = reactive({
  page: 1,
  size: 10
})

const overdueCount = computed(() => {
  return borrowRecords.value.filter(r => r.status === 'OVERDUE').length
})

const emptyText = computed(() => {
  if (activeTab.value === 'BORROWED') return '暂无借阅中的图书'
  if (activeTab.value === 'OVERDUE') return '暂无逾期记录'
  if (activeTab.value === 'RETURNED') return '暂无已归还记录'
  return '暂无借阅记录'
})

const statusText = (status) => {
  const map = { BORROWED: '借阅中', RETURNED: '已归还', OVERDUE: '已逾期' }
  return map[status] || status
}

const statusType = (status) => {
  const map = { BORROWED: 'primary', RETURNED: 'success', OVERDUE: 'danger' }
  return map[status] || 'info'
}

const loadUserInfo = () => {
  request.get('/users/info').then(res => {
    userInfo.value = res.data
  }).catch(() => {})
}

const loadBorrowRecords = () => {
  borrowLoading.value = true
  const params = { ...borrowPage }
  if (activeTab.value) {
    params.status = activeTab.value
  }
  request.get('/borrow/my', { params }).then(res => {
    borrowRecords.value = res.data.records
    borrowTotal.value = res.data.total
  }).catch(() => {}).finally(() => {
    borrowLoading.value = false
  })
}

const handleBorrowPageChange = (page) => {
  borrowPage.page = page
  loadBorrowRecords()
}

const handleRenew = (row) => {
  ElMessageBox.confirm('确定要续借这本图书吗？续借将延长30天。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.put(`/borrow/renew/${row.id}`).then(() => {
      ElMessage.success('续借成功')
      loadBorrowRecords()
      loadUserInfo()
    })
  }).catch(() => {})
}

const handlePayFine = (row) => {
  ElMessageBox.confirm(`确定要缴纳 ¥${row.fineAmount} 罚款吗？\n注意：缴纳罚款后仍需联系管理员归还图书，借阅状态才会变为已归还。`, '提示', {
    confirmButtonText: '确定缴纳',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.put(`/borrow/fine/${row.id}`).then(() => {
      ElMessage.success('罚款缴纳成功，请联系管理员归还图书')
      loadBorrowRecords()
    })
  }).catch(() => {})
}

onMounted(() => {
  loadUserInfo()
  loadBorrowRecords()
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
.stat-value.text-warning {
  color: #f56c6c;
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
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
