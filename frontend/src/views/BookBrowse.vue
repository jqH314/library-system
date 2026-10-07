<template>
  <div class="book-browse-page">
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input v-model="searchForm.keyword" placeholder="搜索书名、作者、ISBN..." clearable style="width: 320px" @keyup.enter="loadBooks">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable style="width: 160px" @change="loadBooks">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-button type="primary" @click="loadBooks">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <div class="book-grid" v-loading="loading">
      <div v-for="book in bookList" :key="book.id" class="book-card" @click="goToDetail(book.id)">
        <div class="book-cover">
          <div class="cover-placeholder">
            <el-icon :size="48"><Reading /></el-icon>
          </div>
          <div class="book-status" :class="book.status === 1 && book.availableCount > 0 ? 'available' : 'unavailable'">
            {{ book.status === 0 ? '已下架' : (book.availableCount > 0 ? '可借' : '已借出') }}
          </div>
        </div>
        <div class="book-info">
          <h3 class="book-title" :title="book.name">{{ book.name }}</h3>
          <p class="book-author">{{ book.author }}</p>
          <p class="book-meta">
            <span>{{ book.publisher }}</span>
          </p>
          <div class="book-footer">
            <span class="available-count">
              可借 <strong :class="book.availableCount > 0 ? 'text-primary' : 'text-danger'">{{ book.availableCount }}</strong> / {{ book.totalCount }}
            </span>
            <el-button 
              size="small" 
              type="primary" 
              :disabled="book.status === 0 || book.availableCount <= 0"
              @click.stop="handleBorrow(book)"
            >
              借阅
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && bookList.length === 0" description="没有找到相关图书" />
    </div>

    <el-pagination
      class="pagination"
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="searchForm.size"
      :current-page="searchForm.page"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const loading = ref(false)
const bookList = ref([])
const total = ref(0)
const categories = ref([])

const searchForm = reactive({
  page: 1,
  size: 12,
  keyword: '',
  categoryId: null,
  status: 1
})

const loadCategories = () => {
  request.get('/categories/list').then(res => {
    categories.value = res.data.filter(c => c.status === 1)
  }).catch(() => {})
}

const loadBooks = () => {
  loading.value = true
  request.get('/books/page', { params: searchForm }).then(res => {
    bookList.value = res.data.records
    total.value = res.data.total
  }).catch(() => {}).finally(() => {
    loading.value = false
  })
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.categoryId = null
  searchForm.page = 1
  loadBooks()
}

const handlePageChange = (page) => {
  searchForm.page = page
  loadBooks()
}

const goToDetail = (id) => {
  router.push(`/books/${id}`)
}

const handleBorrow = (book) => {
  ElMessageBox.confirm(
    `确定要借阅《${book.name}》吗？\n借阅期限30天，可续借2次。`,
    '确认借阅',
    {
      confirmButtonText: '确定借阅',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    request.post(`/borrow/${book.id}`).then(res => {
      ElMessage.success(`借阅成功！应还日期：${res.data.dueTime ? res.data.dueTime.substring(0, 10) : ''}`)
      loadBooks()
    })
  }).catch(() => {})
}

onMounted(() => {
  loadCategories()
  loadBooks()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
}
.search-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  min-height: 400px;
}
.book-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
}
.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
.book-cover {
  position: relative;
  height: 160px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-placeholder {
  color: rgba(255, 255, 255, 0.6);
}
.book-status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}
.book-status.available {
  background: #67c23a;
  color: #fff;
}
.book-status.unavailable {
  background: #909399;
  color: #fff;
}
.book-info {
  padding: 16px;
}
.book-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.book-author {
  margin: 0 0 4px;
  font-size: 13px;
  color: #606266;
}
.book-meta {
  margin: 0 0 12px;
  font-size: 12px;
  color: #909399;
}
.book-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.available-count {
  font-size: 13px;
  color: #606266;
}
.available-count strong {
  font-size: 16px;
}
.text-primary {
  color: #409eff;
}
.text-danger {
  color: #f56c6c;
}
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
