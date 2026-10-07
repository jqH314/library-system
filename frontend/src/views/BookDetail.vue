<template>
  <div class="book-detail-page" v-loading="loading">
    <el-page-header @back="goBack" content="返回图书列表" class="page-header" />
    
    <el-card shadow="never" class="detail-card" v-if="book">
      <el-row :gutter="40">
        <el-col :span="8">
          <div class="book-cover-large">
            <div class="cover-inner">
              <el-icon :size="80"><Reading /></el-icon>
              <div class="cover-title">{{ book.name }}</div>
            </div>
            <div class="book-status-large" :class="book.status === 1 && book.availableCount > 0 ? 'available' : 'unavailable'">
              {{ book.status === 0 ? '已下架' : (book.availableCount > 0 ? '可借阅' : '已借出') }}
            </div>
          </div>
        </el-col>
        <el-col :span="16">
          <h1 class="book-title">{{ book.name }}</h1>
          <div class="book-meta-grid">
            <div class="meta-item">
              <span class="meta-label">作者</span>
              <span class="meta-value">{{ book.author }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">出版社</span>
              <span class="meta-value">{{ book.publisher }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">ISBN</span>
              <span class="meta-value">{{ book.isbn }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">分类</span>
              <span class="meta-value">{{ categoryName }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">价格</span>
              <span class="meta-value">¥{{ book.price }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">馆藏位置</span>
              <span class="meta-value">{{ book.location || '未设置' }}</span>
            </div>
          </div>
          
          <div class="stock-info">
            <div class="stock-item">
              <div class="stock-value">{{ book.totalCount }}</div>
              <div class="stock-label">总馆藏</div>
            </div>
            <div class="stock-divider"></div>
            <div class="stock-item">
              <div class="stock-value" :class="book.availableCount > 0 ? 'text-primary' : 'text-danger'">{{ book.availableCount }}</div>
              <div class="stock-label">可借数量</div>
            </div>
            <div class="stock-divider"></div>
            <div class="stock-item">
              <div class="stock-value">{{ book.borrowCount }}</div>
              <div class="stock-label">累计借阅</div>
            </div>
          </div>

          <div class="action-area">
            <el-button 
              type="primary" 
              size="large" 
              :disabled="book.status === 0 || book.availableCount <= 0"
              @click="handleBorrow"
              style="min-width: 160px"
            >
              <el-icon><Reading /></el-icon> 立即借阅
            </el-button>
            <el-button size="large" @click="goBack">返回列表</el-button>
            <p class="borrow-tip" v-if="book.status === 1 && book.availableCount > 0">
              借阅期限30天，最多可续借2次，逾期罚款0.5元/天
            </p>
            <p class="borrow-tip text-danger" v-else-if="book.status === 0">
              该图书已下架，暂不可借阅
            </p>
            <p class="borrow-tip text-danger" v-else>
              该图书已全部借出，请稍后再试
            </p>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="desc-card" v-if="book">
      <template #header>
        <span class="card-title">图书简介</span>
      </template>
      <div class="book-description">
        {{ book.description || '暂无简介' }}
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const book = ref(null)
const categories = ref([])

const categoryName = computed(() => {
  if (!book.value || !categories.value.length) return '-'
  const cat = categories.value.find(c => c.id === book.value.categoryId)
  return cat ? cat.name : '-'
})

const loadBook = () => {
  loading.value = true
  const bookId = route.params.id
  request.get(`/books/${bookId}`).then(res => {
    book.value = res.data
  }).catch(() => {
    ElMessage.error('图书不存在')
    router.push('/books')
  }).finally(() => {
    loading.value = false
  })
}

const loadCategories = () => {
  request.get('/categories/list').then(res => {
    categories.value = res.data
  }).catch(() => {})
}

const goBack = () => {
  router.push('/books')
}

const handleBorrow = () => {
  ElMessageBox.confirm(
    `确定要借阅《${book.value.name}》吗？\n借阅期限30天，可续借2次，逾期罚款0.5元/天。`,
    '确认借阅',
    {
      confirmButtonText: '确定借阅',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    request.post(`/borrow/${book.value.id}`).then(res => {
      ElMessage.success(`借阅成功！应还日期：${res.data.dueTime ? res.data.dueTime.substring(0, 10) : ''}`)
      loadBook()
    })
  }).catch(() => {})
}

onMounted(() => {
  loadCategories()
  loadBook()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}
.detail-card {
  margin-bottom: 20px;
}
.book-cover-large {
  position: relative;
  height: 320px;
  border-radius: 8px;
  overflow: hidden;
}
.cover-inner {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.7);
  gap: 16px;
}
.cover-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  text-align: center;
  padding: 0 20px;
}
.book-status-large {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
}
.book-status-large.available {
  background: #67c23a;
  color: #fff;
}
.book-status-large.unavailable {
  background: #909399;
  color: #fff;
}
.book-title {
  margin: 0 0 20px;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
.book-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.meta-label {
  font-size: 12px;
  color: #909399;
}
.meta-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.stock-info {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
}
.stock-item {
  flex: 1;
  text-align: center;
}
.stock-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stock-value.text-primary {
  color: #409eff;
}
.stock-value.text-danger {
  color: #f56c6c;
}
.stock-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.stock-divider {
  width: 1px;
  height: 48px;
  background: #e4e7ed;
}
.action-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.borrow-tip {
  font-size: 12px;
  color: #909399;
  margin: 0;
}
.borrow-tip.text-danger {
  color: #f56c6c;
}
.desc-card {
  margin-bottom: 20px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
}
.book-description {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}
</style>
