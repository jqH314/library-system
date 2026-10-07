const app = getApp()

Page({
  data: {
    categories: [],
    hotBooks: [],
    newBooks: [],
    loading: true
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    this.loadData()
  },

  onPullDownRefresh() {
    this.loadData()
    wx.stopPullDownRefresh()
  },

  loadData() {
    this.setData({ loading: true })
    Promise.all([
      app.request({ url: '/categories/list' }),
      app.request({ url: '/books/hot', data: { limit: 6 } }),
      app.request({ url: '/books/new', data: { limit: 6 } })
    ]).then(([catRes, hotRes, newRes]) => {
      this.setData({
        categories: catRes.data.slice(0, 8),
        hotBooks: hotRes.data,
        newBooks: newRes.data,
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  goSearch() {
    wx.switchTab({ url: '/pages/search/search' })
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/detail/detail?id=${id}` })
  },

  goCategory(e) {
    const id = e.currentTarget.dataset.id
    wx.switchTab({ url: '/pages/search/search' })
  }
})
