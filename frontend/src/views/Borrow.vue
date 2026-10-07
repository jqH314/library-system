<template>
  <div class="borrow-page">
    <el-card shadow="never">
      <div class="search-bar">
        <el-input v-model="searchForm.keyword" placeholder="用户名/书名" clearable style="width: 200px" @keyup.enter="loadData" />
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="借阅中" value="BORROWED" />
          <el-option label="已归还" value="RETURNED" />
          <el-option label="已逾期" value="OVERDUE" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="bookId" label="图书ID" width="90" />
        <el-table-column prop="borrowTime" label="借阅时间" width="170" />
        <el-table-column prop="dueTime" label="应还时间" width="170" />
        <el-table-column prop="returnTime" label="归还时间" width="170">
          <template #default="{ row }">
            {{ row.returnTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
            <div v-if="row.status === 'OVERDUE' && row.finePaid === 1" style="font-size: 11px; color: #67c23a; margin-top: 2px">罚款已缴</div>
            <div v-if="row.status === 'OVERDUE' && row.finePaid === 0" style="font-size: 11px; color: #f56c6c; margin-top: 2px">罚款未缴</div>
          </template>
        </el-table-column>
        <el-table-column prop="renewCount" label="续借次数" width="90" align="center" />
        <el-table-column prop="fineAmount" label="罚款" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.fineAmount > 0 && row.finePaid === 0" style="color: #f56c6c">¥{{ row.fineAmount }}</span>
            <span v-else-if="row.fineAmount > 0 && row.finePaid === 1" style="color: #67c23a">已缴 ¥{{ row.fineAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'BORROWED'" size="small" type="success" @click="handleReturn(row)">归还</el-button>
            <el-button v-if="row.status === 'BORROWED'" size="small" @click="handleRenew(row)">续借</el-button>
            <el-button v-if="row.status === 'OVERDUE'" size="small" type="success" @click="handleReturn(row)">归还</el-button>
            <el-button v-if="row.status === 'OVERDUE' && row.finePaid === 0" size="small" type="warning" @click="handlePayFine(row)">缴罚款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50]"
        :page-size="searchForm.size"
        :current-page="searchForm.page"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const searchForm = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: null
})

const statusText = (status) => {
  const map = { BORROWED: '借阅中', RETURNED: '已归还', OVERDUE: '已逾期' }
  return map[status] || status
}

const statusType = (status) => {
  const map = { BORROWED: 'primary', RETURNED: 'success', OVERDUE: 'danger' }
  return map[status] || 'info'
}

const loadData = () => {
  loading.value = true
  request.get('/borrow/admin/page', { params: searchForm }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).catch(() => {}).finally(() => {
    loading.value = false
  })
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = null
  searchForm.page = 1
  loadData()
}

const handlePageChange = (page) => {
  searchForm.page = page
  loadData()
}

const handleSizeChange = (size) => {
  searchForm.size = size
  searchForm.page = 1
  loadData()
}

const handleReturn = (row) => {
  ElMessageBox.confirm('确定要归还这本图书吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.put(`/borrow/admin/return/${row.id}`).then(() => {
      ElMessage.success('归还成功')
      loadData()
    })
  }).catch(() => {})
}

const handleRenew = (row) => {
  ElMessageBox.confirm('确定要续借这本图书吗？续借将延长30天。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.put(`/borrow/admin/renew/${row.id}`).then(() => {
      ElMessage.success('续借成功')
      loadData()
    })
  }).catch(() => {})
}

const handlePayFine = (row) => {
  ElMessageBox.confirm(`确定要缴纳 ¥${row.fineAmount} 罚款吗？\n注意：缴纳罚款后仍需归还图书，借阅状态才会变为已归还。`, '提示', {
    confirmButtonText: '确定缴纳',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.put(`/borrow/admin/fine/${row.id}`).then(() => {
      ElMessage.success('罚款缴纳成功，请及时归还图书')
      loadData()
    })
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
