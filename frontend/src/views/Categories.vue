<template>
  <div class="categories-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增分类
        </el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" row-key="id">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="180" />
        <el-table-column prop="code" label="分类编码" width="140" />
        <el-table-column prop="parentId" label="父分类ID" width="120" align="center">
          <template #default="{ row }">
            {{ row.parentId === 0 ? '一级分类' : row.parentId }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleMoreAction(cmd, row)">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="addChild">添加子分类</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入分类编码，如 CS-PL" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="form.parentId" style="width: 100%" clearable placeholder="不选则为一级分类">
            <el-option v-for="cat in parentCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import request from '@/api/request'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const isAddChild = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  name: '',
  code: '',
  parentId: 0,
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入分类编码', trigger: 'blur' }]
}

const parentCategories = computed(() => {
  return tableData.value.filter(c => c.parentId === 0)
})

const dialogTitle = computed(() => {
  if (isEdit.value) return '编辑分类'
  if (isAddChild.value) return '添加子分类'
  return '新增分类'
})

const loadData = () => {
  loading.value = true
  request.get('/categories/list').then(res => {
    tableData.value = res.data
  }).catch(() => {}).finally(() => {
    loading.value = false
  })
}

const handleAdd = () => {
  isEdit.value = false
  isAddChild.value = false
  Object.assign(form, { id: null, name: '', code: '', parentId: 0, sort: 0, status: 1 })
  dialogVisible.value = true
}

const handleAddChild = (row) => {
  isEdit.value = false
  isAddChild.value = true
  Object.assign(form, { id: null, name: '', code: '', parentId: row.id, sort: 0, status: 1 })
  dialogVisible.value = true
}

const handleMoreAction = (cmd, row) => {
  if (cmd === 'addChild') {
    handleAddChild(row)
  }
}

const handleEdit = (row) => {
  isEdit.value = true
  isAddChild.value = false
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const api = isEdit.value ? request.put('/categories', form) : request.post('/categories', form)
      api.then(() => {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        loadData()
      })
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除分类「${row.name}」吗？如有子分类也会受影响。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request.delete(`/categories/${row.id}`).then(() => {
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
.toolbar {
  margin-bottom: 20px;
}
</style>
