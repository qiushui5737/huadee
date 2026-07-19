<template>
  <div class="interaction-page">
    <!-- 顶部横幅 -->
    <div class="hero-banner">
      <div class="banner-content">
        <div class="banner-title">
          <h1>政民互动</h1>
          <span class="location-tag">
            <el-icon><Location /></el-icon>
            四川省
          </span>
        </div>
        <div class="nav-icons">
          <div class="nav-item" v-for="item in navItems" :key="item.label" @click="handleNavClick(item)">
            <div class="nav-icon-wrapper">
              <el-icon :size="32"><component :is="item.icon" /></el-icon>
            </div>
            <span class="nav-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="main-content">
      <el-row :gutter="24">
        <!-- 左侧：省长信箱 -->
        <el-col :span="14">
          <div class="section-card">
            <div class="section-header">
              <h2 class="section-title">省长信箱</h2>
              <div class="section-links">
                <a href="javascript:void(0)" @click="showWriteLetter">我要写信</a>
                <span class="divider">/</span>
                <a href="javascript:void(0)" @click="showQueryLetter">查询信件</a>
              </div>
            </div>
            <div class="section-divider"></div>

            <h3 class="subsection-title">来信选登</h3>

            <div class="letter-list">
              <div v-if="letterList.length === 0" class="empty-state">
                <el-empty description="暂无来信" />
              </div>
              <div
                v-for="item in letterList"
                :key="item.id"
                class="letter-item"
                @click="showLetterDetail(item)"
              >
                <el-icon class="letter-icon"><Message /></el-icon>
                <span class="letter-title">{{ item.title }}</span>
                <span class="letter-date">{{ item.createTime }}</span>
              </div>
            </div>

            <div class="letter-stats" v-if="letterStats">
              受理范围的{{ letterStats }}件群众来信进行了办理。
            </div>
          </div>
        </el-col>

        <!-- 右侧：在线访谈 -->
        <el-col :span="10">
          <div class="section-card">
            <div class="section-header">
              <h2 class="section-title">在线访谈</h2>
              <div class="section-links">
                <a href="javascript:void(0)" @click="showPastInterviews">往期访谈</a>
                <span class="divider">/</span>
                <a href="javascript:void(0)" @click="showAskQuestion">我要提问</a>
              </div>
            </div>
            <div class="section-divider"></div>

            <div class="interview-card" v-if="currentInterview">
              <div class="interview-image">
                <img :src="currentInterview.image" :alt="currentInterview.topic" />
              </div>
              <div class="interview-info">
                <div class="interview-topic">
                  <el-icon class="topic-icon"><Microphone /></el-icon>
                  <strong>访谈主题：</strong>{{ currentInterview.topic }}
                </div>
                <div class="interview-guest">
                  <el-icon class="guest-icon"><User /></el-icon>
                  <strong>访谈嘉宾：</strong>{{ currentInterview.guest }}
                </div>
                <div class="interview-background">
                  <el-icon class="bg-icon"><Document /></el-icon>
                  <strong>访谈背景：</strong>{{ currentInterview.background }}
                </div>
              </div>
            </div>

            <div v-else class="empty-state">
              <el-empty description="暂无在线访谈" />
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 意见征集 -->
      <div class="section-card" style="margin-top: 24px;">
        <div class="section-header">
          <h2 class="section-title">意见征集</h2>
          <div class="section-links">
            <a href="javascript:void(0)" @click="showMoreCollection">更多征集</a>
            <span class="divider">/</span>
            <a href="javascript:void(0)" @click="showResultFeedback">结果反馈</a>
          </div>
        </div>
        <div class="section-divider"></div>

        <div class="collection-list">
          <div v-if="collectionList.length === 0" class="empty-state">
            <el-empty description="暂无意见征集" />
          </div>
          <div
            v-for="item in collectionList"
            :key="item.id"
            class="collection-item"
            @click="showCollectionDetail(item)"
          >
            <el-tag :type="item.status === '进行中' ? 'danger' : 'info'" size="small" class="status-tag">
              {{ item.status }}
            </el-tag>
            <div class="collection-content">
              <div class="collection-title">{{ item.title }}</div>
              <div class="collection-desc">{{ item.description }}</div>
            </div>
            <div class="collection-date">{{ item.startDate }} ~ {{ item.endDate }}</div>
          </div>
        </div>
      </div>
    </div>



    <!-- 查询信件弹窗 -->
    <el-dialog v-model="queryLetterVisible" title="查询信件" width="650px">
      <div style="display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap;">
        <el-input v-model="queryConsultNo" placeholder="输入信件单号精确查询" style="width: 220px;" @keyup.enter="doQueryLetter" />
        <el-input v-model="queryKeyword" placeholder="输入标题关键词模糊查询" style="flex: 1;" @keyup.enter="doQueryLetter" />
        <el-button type="primary" @click="doQueryLetter">查询</el-button>
      </div>
      <div v-if="queryResults.length > 0">
        <div v-for="item in queryResults" :key="item.id" class="letter-item" @click="queryLetterVisible = false; showLetterDetail(item)" style="cursor: pointer; padding: 12px 0; border-bottom: 1px solid #f0f0f0; display: flex; align-items: center;">
          <el-icon style="color: #409eff; margin-right: 12px;"><Message /></el-icon>
          <span style="flex: 1;">{{ item.title }}</span>
          <el-tag v-if="!item.isPublic" type="warning" size="small" style="margin-right: 8px;">私密</el-tag>
          <el-tag :type="statusTagType(item.status)" size="small">{{ item.status }}</el-tag>
          <span style="margin-left: 12px; color: #999; font-size: 13px;">{{ item.consultNo }}</span>
        </div>
      </div>
      <el-empty v-else-if="querySearched" description="未找到相关信件" />
      <el-empty v-else description="输入单号或关键词后点击查询" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Location, ChatDotRound, EditPen, Phone, Message, VideoCamera, Document,
  Microphone, User
} from '@element-plus/icons-vue'
import {
  submitMessage, messageList as fetchMessageList
} from '@/api/interaction'

const router = useRouter()

// 导航图标
const navItems = [
  { label: '我要咨询', icon: ChatDotRound, action: 'consult' },
  { label: '我要建议', icon: EditPen, action: 'suggest' },
  { label: '我要投诉', icon: Phone, action: 'complain' },
  { label: '省长信箱', icon: Message, action: 'mailbox' },
  { label: '在线访谈', icon: VideoCamera, action: 'interview' },
  { label: '意见征集', icon: Document, action: 'collection' }
]

// 来信列表
const letterList = ref<any[]>([])
const letterStats = ref(0)

// 在线访谈（模拟数据）
const currentInterview = ref({
  image: 'https://via.placeholder.com/400x250/409EFF/FFFFFF?text=在线访谈',
  topic: '四川：加强监测预警 全力做好气象服务保障',
  guest: '四川省气象局党组成员、副局长 李勇',
  background: '7月1日起，四川进入防汛抗旱关键时段。经气象等部门会商研判，整体来看，今年主汛期四川平均降水量较常年同期偏少，但...'
})

// 意见征集（模拟数据）
const collectionList = ref([
  {
    id: 1,
    title: '《四川省"十五五"重点流域水生态环境保护规划（征求意见稿）》',
    description: '为贯彻落实党的二十届四中全会精神和省委十二届八次全会精神...',
    status: '进行中',
    startDate: '2026-07-01',
    endDate: '2026-08-31'
  },
  {
    id: 2,
    title: '《四川省城市更新实施办法（草案）》公开征求意见',
    description: '为推动城市更新工作高质量发展，改善人居环境...',
    status: '已结束',
    startDate: '2026-05-01',
    endDate: '2026-06-30'
  }
])

// 导航点击
const handleNavClick = (item: any) => {
  if (item.action === 'mailbox') {
    router.push({ name: 'public-letters' })
  } else if (item.action === 'consult') {
    router.push({ name: 'consultation' })
  } else if (item.action === 'suggest') {
    router.push({ name: 'submit-suggest' })
  } else if (item.action === 'complain') {
    router.push({ name: 'submit-complaint' })
  } else if (item.action === 'interview') {
    showAskQuestion()
  } else if (item.action === 'collection') {
    router.push({ name: 'collection-list' })
  }
}

// 写信 - 跳转到写信页面
const showWriteLetter = () => {
  router.push({ name: 'write-letter' })
}

const submitLetter = async () => {
  // 已迁移到 WriteLetter.vue 页面
}

// 查询信件
const queryLetterVisible = ref(false)
const queryKeyword = ref('')
const queryConsultNo = ref('')
const queryResults = ref<any[]>([])
const querySearched = ref(false)
const showQueryLetter = () => {
  queryKeyword.value = ''
  queryConsultNo.value = ''
  queryResults.value = []
  querySearched.value = false
  queryLetterVisible.value = true
}
const doQueryLetter = async () => {
  if (!queryKeyword.value.trim() && !queryConsultNo.value.trim()) {
    ElMessage.warning('请输入信件单号或关键词')
    return
  }
  try {
    const params: any = { page: 1, size: 20 }
    if (queryConsultNo.value.trim()) params.consultNo = queryConsultNo.value.trim()
    if (queryKeyword.value.trim()) params.keyword = queryKeyword.value.trim()
    const res: any = await fetchMessageList(params)
    if (res.code === 200) {
      queryResults.value = res.data.records
      querySearched.value = true
      if (queryResults.value.length === 0) {
        ElMessage.info('未找到相关信件')
      }
    }
  } catch (e) {
    ElMessage.error('查询失败')
  }
}

// 信件详情 - 跳转到详情页
const showLetterDetail = (row: any) => {
  router.push({ name: 'letter-detail', params: { id: row.id } })
}

// 往期访谈
const showPastInterviews = () => {
  ElMessage.info('往期访谈功能开发中')
}

// 我要提问
const showAskQuestion = () => {
  ElMessage.info('在线提问功能开发中')
}

// 更多征集
const showMoreCollection = () => {
  ElMessage.info('更多征集功能开发中')
}

// 结果反馈
const showResultFeedback = () => {
  ElMessage.info('结果反馈功能开发中')
}

// 征集详情
const showCollectionDetail = (item: any) => {
  ElMessage.info(`查看征集详情：${item.title}`)
}

// 加载来信列表
const loadLetters = async () => {
  try {
    const res: any = await fetchMessageList({ page: 1, size: 5 })
    if (res.code === 200) {
      letterList.value = res.data.records
      letterStats.value = res.data.total
    }
  } catch (e) {
    console.error('加载来信列表失败', e)
  }
}

// 标签颜色
const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    '待分派': 'warning',
    '已分派': '',
    '已回复': 'success',
    '已办结': 'info'
  }
  return map[status] || ''
}

const typeTagType = (type: string) => {
  const map: Record<string, string> = {
    '咨询': '',
    '投诉': 'danger',
    '建议': 'success',
    '求助': 'warning'
  }
  return map[type] || ''
}

onMounted(() => {
  loadLetters()
})
</script>

<style scoped lang="scss">
.interaction-page {
  background: #f5f7fa;
  min-height: 100vh;
}

// 顶部横幅
.hero-banner {
  background: linear-gradient(135deg, #1e5799 0%, #2989d8 50%, #207cca 100%);
  padding: 40px 0 30px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><path d="M0,50 Q25,30 50,50 T100,50" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="2"/></svg>');
    background-size: 200px;
    opacity: 0.3;
  }

  .banner-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    position: relative;
    z-index: 1;
  }

  .banner-title {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 30px;

    h1 {
      color: #fff;
      font-size: 36px;
      font-weight: 600;
      margin: 0;
      letter-spacing: 2px;
    }

    .location-tag {
      display: flex;
      align-items: center;
      gap: 4px;
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 14px;
      backdrop-filter: blur(10px);
    }
  }

  .nav-icons {
    display: flex;
    gap: 16px;
    justify-content: flex-end;

    .nav-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      transition: transform 0.3s;

      &:hover {
        transform: translateY(-4px);
      }

      .nav-icon-wrapper {
        width: 70px;
        height: 70px;
        background: #fff;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #409eff;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transition: all 0.3s;
      }

      &:hover .nav-icon-wrapper {
        background: #ecf5ff;
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
      }

      .nav-label {
        color: #fff;
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
}

// 主体内容
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

.section-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .section-title {
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }

    .section-links {
      font-size: 14px;
      color: #909399;

      a {
        color: #409eff;
        text-decoration: none;
        transition: color 0.3s;

        &:hover {
          color: #66b1ff;
        }
      }

      .divider {
        margin: 0 8px;
        color: #dcdfe6;
      }
    }
  }

  .section-divider {
    height: 3px;
    background: linear-gradient(90deg, #409eff 0%, #409eff 30%, transparent 30%);
    margin-bottom: 20px;
  }
}

// 来信列表
.subsection-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-left: 12px;
  border-left: 3px solid #409eff;
}

.letter-list {
  .letter-item {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background 0.3s;

    &:hover {
      background: #f5f7fa;
      padding-left: 8px;
    }

    &:last-child {
      border-bottom: none;
    }

    .letter-icon {
      color: #409eff;
      margin-right: 12px;
      font-size: 18px;
    }

    .letter-title {
      flex: 1;
      font-size: 14px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      &:hover {
        color: #409eff;
      }
    }

    .letter-date {
      font-size: 14px;
      color: #909399;
      margin-left: 16px;
      white-space: nowrap;
    }
  }
}

.letter-stats {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #606266;
}

// 在线访谈
.interview-card {
  .interview-image {
    margin-bottom: 16px;
    border-radius: 8px;
    overflow: hidden;

    img {
      width: 100%;
      height: 200px;
      object-fit: cover;
      display: block;
    }
  }

  .interview-info {
    .interview-topic,
    .interview-guest,
    .interview-background {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 12px;
      font-size: 14px;
      color: #303133;
      line-height: 1.6;

      .topic-icon {
        color: #409eff;
        margin-top: 2px;
      }

      .guest-icon {
        color: #e6a23c;
        margin-top: 2px;
      }

      .bg-icon {
        color: #67c23a;
        margin-top: 2px;
      }

      strong {
        color: #303133;
        white-space: nowrap;
      }
    }
  }
}

// 意见征集
.collection-list {
  .collection-item {
    display: flex;
    align-items: flex-start;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background 0.3s;

    &:hover {
      background: #f5f7fa;
      padding-left: 8px;
    }

    &:last-child {
      border-bottom: none;
    }

    .status-tag {
      margin-right: 16px;
      margin-top: 2px;
    }

    .collection-content {
      flex: 1;

      .collection-title {
        font-size: 15px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 6px;

        &:hover {
          color: #409eff;
        }
      }

      .collection-desc {
        font-size: 13px;
        color: #909399;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .collection-date {
      font-size: 13px;
      color: #909399;
      white-space: nowrap;
      margin-left: 16px;
    }
  }
}

.empty-state {
  padding: 20px 0;
}
</style>
