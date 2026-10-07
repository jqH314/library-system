<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">{{ card.title }}</p>
              <p class="stat-value">{{ card.value }}</p>
            </div>
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon :size="28"><component :is="card.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card title="借阅趋势" shadow="hover">
          <div ref="trendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card title="分类占比" shadow="hover">
          <div ref="pieChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card title="热门图书排行" shadow="hover">
          <el-table :data="hotBooks" stripe>
            <el-table-column type="index" label="排名" width="80" align="center">
              <template #default="{ $index }">
                <el-tag :type="$index < 3 ? 'warning' : 'info'" size="small">{{ $index + 1 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="书名" />
            <el-table-column prop="author" label="作者" width="150" />
            <el-table-column prop="borrowCount" label="借阅次数" width="120" align="center" />
            <el-table-column prop="availableCount" label="可借数量" width="120" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/api/request'

const trendChart = ref()
const pieChart = ref()
const hotBooks = ref([])

const statCards = ref([
  { title: '图书总数', value: 0, icon: 'Reading', color: '#409eff' },
  { title: '借阅中', value: 0, icon: 'Tickets', color: '#67c23a' },
  { title: '读者总数', value: 0, icon: 'User', color: '#e6a23c' },
  { title: '逾期记录', value: 0, icon: 'Warning', color: '#f56c6c' }
])

const initCharts = () => {
  const trend = echarts.init(trendChart.value)
  trend.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [{
      data: [12, 19, 15, 25, 22, 30, 18],
      type: 'line',
      smooth: true,
      areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
      lineStyle: { color: '#409eff', width: 3 },
      itemStyle: { color: '#409eff' }
    }]
  })

  const pie = echarts.init(pieChart.value)
  pie.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: 35, name: '计算机' },
        { value: 25, name: '文学' },
        { value: 18, name: '历史' },
        { value: 12, name: '经济' },
        { value: 10, name: '其他' }
      ],
      label: { formatter: '{b}: {d}%' }
    }]
  })
}

const loadData = () => {
  request.get('/books/page', { params: { page: 1, size: 1 } }).then(res => {
    statCards.value[0].value = res.data.total
  })
  request.get('/users/page', { params: { page: 1, size: 1, role: 'READER' } }).then(res => {
    statCards.value[2].value = res.data.total
  })
  request.get('/books/hot', { params: { limit: 5 } }).then(res => {
    hotBooks.value = res.data
  })
  statCards.value[1].value = 5
  statCards.value[3].value = 1
}

onMounted(() => {
  nextTick(() => {
    initCharts()
    loadData()
  })
})
</script>

<style scoped>
.stat-card {
  border-radius: 8px;
}
.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
</style>
