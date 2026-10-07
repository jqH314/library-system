<template>
  <div class="users-page">
    <el-card shadow="never">
      <div class="search-bar">
        <el-input v-model="searchForm.keyword" placeholder="用户名/姓名/手机号" clearable style="width: 220px" @keyup.enter="loadData" />
        <el-select v-model="searchForm.role" placeholder="全部角色" clearable style="width: 140px">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="读者" value="READER" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="handleAdd" style="margin-left: auto">
          <el-icon><Plus /></el-icon> 新增读者
        </el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名/学号" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '读者' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="借阅情况" width="140" align="center">
          <template #default="{ row }">
            <span>{{ row.borrowedCount }} / {{ row.maxBorrowCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleMoreAction(cmd, row)">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="reset">重置密码</el-dropdown-item>
                  <el-dropdown-item :command="row.status === 1 ? 'disable' : 'enable'">
                    {{ row.status === 1 ? '禁用账号' : '启用账号' }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑读者' : '新增读者'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="学号/工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="读者" value="READER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="最大借阅数" prop="maxBorrowCount">
          <el-input-number v-model="form.maxBorrowCount" :min="1" :max="999" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码">
          <el-input value="123456" disabled />
          <span style="color: #909399; font-size: 12px; margin-left: 8px">新用户默认密码123456</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import request from '@/api/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const searchForm = reactive({
  page: 1,
  size: 10,
  keyword: '',
  role: null
})

const form = reactive({
  id: null,
  username: '',
  realName: '',
  role: 'READER',
  phone: '',
  email: '',
  maxBorrowCount: 5
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const loadData = () => {
  loading.value = true
  request.get('/users/page', { params: searchForm }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).catch(() => {}).finally(() => {
    loading.value = false
  })
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.role = null
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

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, username: '', realName: '', role: 'READER', phone: '', email: '', maxBorrowCount: 5 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const api = isEdit.value ? request.put('/users', form) : request.post('/users', form)
      api.then(() => {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        loadData()
      })
    }
  })
}

const handleResetPassword = (row) => {
  ElMessageBox.confirm(`确定要重置「${row.realName}」的密码为123456吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.put(`/users/${row.id}/reset-password`).then(() => {
      ElMessage.success('密码重置成功')
    })
  }).catch(() => {})
}

const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  request.put(`/users/${row.id}/status`, null, { params: { status: newStatus } }).then(() => {
    ElMessage.success('操作成功')
    loadData()
  })
}

const handleMoreAction = (cmd, row) => {
  if (cmd === 'reset') {
    handleResetPassword(row)
  } else if (cmd === 'disable' || cmd === 'enable') {
    handleToggleStatus(row)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除「${row.realName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete(`/users/${row.id}`).then(() => {
      ElMessage.success('删除成功')
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
