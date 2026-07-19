<template>
  <div class="write-letter-page">
    <!-- 顶部操作栏 -->
    <div class="top-bar">
      <div class="recipient-row">
        <span class="label">收件人</span>
        <el-icon class="add-icon" @click="showDeptSelector"><Plus /></el-icon>
        <div class="recipient-tags">
          <el-tag
            v-for="dept in selectedDepts"
            :key="dept.value"
            closable
            @close="removeDept(dept.value)"
            class="dept-tag"
          >
            {{ dept.label }}
          </el-tag>
          <span v-if="selectedDepts.length === 0" class="placeholder">请选择目标部门</span>
        </div>
      </div>
      <div class="top-actions">
        <el-switch v-model="form.isPublic" active-text="公开" inactive-text="私密" style="margin-right: 12px;" />
        <span class="action-link">抄送</span>
        <span class="action-link">密送</span>
        <span class="action-divider"></span>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">发送</el-button>
      </div>
    </div>

    <!-- 主题行 -->
    <div class="subject-row">
      <span class="label">主 题</span>
      <el-input
        v-model="form.title"
        placeholder="请输入信件标题"
        class="subject-input"
        maxlength="50"
        show-word-limit
      />
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-row">
        <div class="toolbar-group">
          <button class="tool-btn" @click="execCmd('undo')" title="撤销">
            <el-icon><RefreshLeft /></el-icon>
          </button>
          <button class="tool-btn" @click="execCmd('redo')" title="重做">
            <el-icon><RefreshRight /></el-icon>
          </button>
        </div>
        <div class="toolbar-group">
          <button class="tool-btn" @click="insertImage" title="图片">
            <el-icon><Picture /></el-icon>
            <span>图片</span>
          </button>
          <el-dropdown trigger="click" @command="handleInsert">
            <button class="tool-btn">
              <el-icon><CirclePlus /></el-icon>
              <span>插入</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="table">表格</el-dropdown-item>
                <el-dropdown-item command="link">链接</el-dropdown-item>
                <el-dropdown-item command="file">附件</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <button class="tool-btn" title="导入文档">
            <el-icon><Document /></el-icon>
            <span>导入文档</span>
            <el-icon class="arrow"><ArrowDown /></el-icon>
          </button>
          <button class="tool-btn" title="日程">
            <el-icon><Calendar /></el-icon>
            <span>日程</span>
          </button>
          <button class="tool-btn" title="表情">
            <el-icon><ChatLineRound /></el-icon>
            <span>表情</span>
          </button>
        </div>
        <div class="toolbar-group format-group">
          <button class="tool-btn active" title="格式">
            <span class="format-text">Aa 格式</span>
            <el-icon class="arrow"><ArrowUp /></el-icon>
          </button>
        </div>
        <div class="toolbar-group right-group">
          <el-dropdown trigger="click">
            <button class="tool-btn">
              <el-icon><EditPen /></el-icon>
              <span>签名</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>默认签名</el-dropdown-item>
                <el-dropdown-item>管理签名</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <button class="tool-btn" title="更多">
            <el-icon><MoreFilled /></el-icon>
          </button>
        </div>
      </div>

      <!-- 格式工具栏 -->
      <div class="format-toolbar" v-if="showFormatToolbar">
        <div class="toolbar-group">
          <button class="tool-btn-sm" title="清除格式">
            <el-icon><Brush /></el-icon>
          </button>
          <button class="tool-btn-sm" title="清除样式">
            <el-icon><Brush /></el-icon>
          </button>
        </div>
        <div class="toolbar-group">
          <el-dropdown trigger="click" @command="handleFont">
            <button class="tool-btn-sm">默认字体 <el-icon class="arrow"><ArrowDown /></el-icon></button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="宋体">宋体</el-dropdown-item>
                <el-dropdown-item command="微软雅黑">微软雅黑</el-dropdown-item>
                <el-dropdown-item command="黑体">黑体</el-dropdown-item>
                <el-dropdown-item command="楷体">楷体</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click" @command="handleFontSize">
            <button class="tool-btn-sm">字号 <el-icon class="arrow"><ArrowDown /></el-icon></button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="1">小</el-dropdown-item>
                <el-dropdown-item command="3">正常</el-dropdown-item>
                <el-dropdown-item command="5">大</el-dropdown-item>
                <el-dropdown-item command="7">特大</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="toolbar-group">
          <button class="tool-btn-sm" @click="execCmd('bold')" title="加粗"><strong>B</strong></button>
          <button class="tool-btn-sm" @click="execCmd('italic')" title="斜体"><em>I</em></button>
          <button class="tool-btn-sm" @click="execCmd('underline')" title="下划线"><u>U</u></button>
          <button class="tool-btn-sm" @click="execCmd('strikeThrough')" title="删除线"><s>S</s></button>
        </div>
        <div class="toolbar-group">
          <el-dropdown trigger="click" @command="(cmd: string) => execCmdArg('foreColor', cmd)">
            <button class="tool-btn-sm" title="字体颜色">
              <span class="color-indicator" style="border-bottom: 2px solid #f56c6c;">A</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="#000000">黑色</el-dropdown-item>
                <el-dropdown-item command="#f56c6c">红色</el-dropdown-item>
                <el-dropdown-item command="#409eff">蓝色</el-dropdown-item>
                <el-dropdown-item command="#67c23a">绿色</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click" @command="(cmd: string) => execCmdArg('hiliteColor', cmd)">
            <button class="tool-btn-sm" title="背景色">
              <span class="highlight-indicator"></span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="#ffff00">黄色</el-dropdown-item>
                <el-dropdown-item command="#00ff00">绿色</el-dropdown-item>
                <el-dropdown-item command="#00ffff">青色</el-dropdown-item>
                <el-dropdown-item command="transparent">无</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="toolbar-group">
          <button class="tool-btn-sm" @click="execCmd('insertUnorderedList')" title="无序列表">
            <el-icon><List /></el-icon>
          </button>
          <button class="tool-btn-sm" @click="execCmd('insertOrderedList')" title="有序列表">
            <el-icon><Sort /></el-icon>
          </button>
          <button class="tool-btn-sm" @click="execCmd('indent')" title="增加缩进">
            <el-icon><DArrowRight /></el-icon>
          </button>
          <button class="tool-btn-sm" @click="execCmd('outdent')" title="减少缩进">
            <el-icon><DArrowLeft /></el-icon>
          </button>
        </div>
        <div class="toolbar-group">
          <button class="tool-btn-sm" @click="execCmd('justifyLeft')" title="左对齐">
            <el-icon><Menu /></el-icon>
          </button>
          <button class="tool-btn-sm" @click="execCmd('justifyCenter')" title="居中">
            <el-icon><Grid /></el-icon>
          </button>
          <button class="tool-btn-sm" @click="execCmd('justifyRight')" title="右对齐">
            <el-icon><Fold /></el-icon>
          </button>
        </div>
        <div class="toolbar-group">
          <button class="tool-btn-sm" @click="execCmd('formatBlock', 'blockquote')" title="引用">
            <span class="quote-icon">❝</span>
          </button>
          <button class="tool-btn-sm" @click="insertCode" title="代码">
            <span class="code-icon">&lt;/&gt;</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 正文编辑区 -->
    <div class="editor-container">
      <div
        ref="editorRef"
        class="editor-content"
        contenteditable="true"
        @compositionstart="isComposing = true"
        @compositionend="onCompositionEnd"
        @blur="syncContent"
        placeholder="输入正文"
      ></div>
    </div>

    <!-- 底部用户信息 -->
    <div class="user-info">
      <div class="user-avatar">
        <el-avatar :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
      </div>
      <div class="user-detail">
        <div class="user-name">{{ userInfo.name }}</div>
        <div class="user-email">{{ userInfo.email }}</div>
      </div>
    </div>

    <!-- 部门选择弹窗 -->
    <el-dialog v-model="deptSelectorVisible" title="选择目标部门" width="500px">
      <el-select v-model="tempDept" placeholder="请选择部门" style="width: 100%">
        <el-option label="省政府办公厅" value="GOV" />
        <el-option label="教育厅" value="EDU" />
        <el-option label="卫健委" value="HEA" />
        <el-option label="住建厅" value="HOU" />
        <el-option label="人社厅" value="SOC" />
        <el-option label="公安厅" value="POL" />
        <el-option label="财政厅" value="FIN" />
        <el-option label="交通厅" value="TRA" />
      </el-select>
      <template #footer>
        <el-button @click="deptSelectorVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDept">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Plus, RefreshLeft, RefreshRight, Picture, CirclePlus, Document, Calendar,
  EditPen, MoreFilled, ArrowDown, ArrowUp, Brush, List, Sort,
  ChatLineRound, DArrowRight, DArrowLeft, Menu, Grid, Fold
} from '@element-plus/icons-vue'
import { submitMessage } from '@/api/interaction'

const router = useRouter()
const editorRef = ref<HTMLElement>()
const submitting = ref(false)
const showFormatToolbar = ref(true)
const deptSelectorVisible = ref(false)
const tempDept = ref('')
const isComposing = ref(false)

// 部门选项
const deptOptions: Record<string, string> = {
  GOV: '省政府办公厅',
  EDU: '教育厅',
  HEA: '卫健委',
  HOU: '住建厅',
  SOC: '人社厅',
  POL: '公安厅',
  FIN: '财政厅',
  TRA: '交通厅'
}

const selectedDepts = ref<{ label: string; value: string }[]>([])

// 表单
const form = reactive({
  title: '',
  content: '',
  contactName: '',
  targetDept: '',
  type: '咨询',
  isPublic: true
})

// 用户信息（从localStorage或store获取）
const userInfo = reactive({
  name: localStorage.getItem('user_name') || '市民用户',
  email: localStorage.getItem('user_email') || 'user@gov.cn'
})

// 显示部门选择器
const showDeptSelector = () => {
  tempDept.value = ''
  deptSelectorVisible.value = true
}

const confirmDept = () => {
  if (tempDept.value && deptOptions[tempDept.value]) {
    if (!selectedDepts.value.find(d => d.value === tempDept.value)) {
      selectedDepts.value.push({
        label: deptOptions[tempDept.value],
        value: tempDept.value
      })
      form.targetDept = selectedDepts.value.map(d => d.value).join(',')
    }
  }
  deptSelectorVisible.value = false
}

const removeDept = (value: string) => {
  selectedDepts.value = selectedDepts.value.filter(d => d.value !== value)
  form.targetDept = selectedDepts.value.map(d => d.value).join(',')
}

// 富文本命令
const execCmd = (command: string, value?: string) => {
  document.execCommand(command, false, value)
  editorRef.value?.focus()
}

const execCmdArg = (command: string, value: string) => {
  document.execCommand(command, false, value)
  editorRef.value?.focus()
}

const handleFont = (font: string) => {
  document.execCommand('fontName', false, font)
  editorRef.value?.focus()
}

const handleFontSize = (size: string) => {
  document.execCommand('fontSize', false, size)
  editorRef.value?.focus()
}

const insertImage = () => {
  const url = prompt('请输入图片URL：')
  if (url) {
    document.execCommand('insertImage', false, url)
  }
}

const insertCode = () => {
  const code = prompt('请输入代码：')
  if (code) {
    document.execCommand('insertHTML', false, `<pre style="background:#f5f5f5;padding:10px;border-radius:4px;font-family:monospace;">${code}</pre><p><br></p>`)
  }
}

const handleInsert = (command: string) => {
  if (command === 'table') {
    const html = '<table border="1" style="border-collapse:collapse;width:100%;"><tr><td style="border:1px solid #ddd;padding:8px;">单元格1</td><td style="border:1px solid #ddd;padding:8px;">单元格2</td></tr><tr><td style="border:1px solid #ddd;padding:8px;">单元格3</td><td style="border:1px solid #ddd;padding:8px;">单元格4</td></tr></table><p><br></p>'
    document.execCommand('insertHTML', false, html)
  } else if (command === 'link') {
    const url = prompt('请输入链接URL：')
    if (url) {
      document.execCommand('createLink', false, url)
    }
  } else if (command === 'file') {
    ElMessage.info('附件功能开发中')
  }
  editorRef.value?.focus()
}

// 输入处理：仅在失焦时同步，避免响应式更新干扰 contenteditable 光标
const syncContent = () => {
  if (editorRef.value) {
    form.content = editorRef.value.innerHTML
  }
}

const onCompositionEnd = () => {
  isComposing.value = false
}

// 提交
const handleSubmit = async () => {
  // 先同步编辑器内容（点击按钮时编辑器可能未失焦）
  syncContent()
  if (!form.title.trim()) {
    ElMessage.warning('请输入信件标题')
    return
  }
  if (!editorRef.value || !editorRef.value.innerText.trim()) {
    ElMessage.warning('请输入信件内容')
    return
  }

  submitting.value = true
  try {
    const res: any = await submitMessage({
      ...form,
      contactName: userInfo.name
    })
    if (res.code === 200) {
      const consultNo = res.data?.consultNo || ''
      ElMessage.success(`信件发送成功！信件单号：${consultNo}`)
      router.push('/interaction')
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e) {
    ElMessage.error('网络异常，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 设置默认签名
  if (editorRef.value) {
    editorRef.value.innerHTML = `<p>此致</p><p>敬礼</p><p><br></p><p>${userInfo.name}</p><p>${new Date().toLocaleDateString('zh-CN')}</p>`
  }
})
</script>

<style scoped lang="scss">
.write-letter-page {
  background: #fff;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

// 顶部操作栏
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #e8e8e8;

  .recipient-row {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;

    .label {
      font-size: 14px;
      color: #409eff;
      font-weight: 500;
      white-space: nowrap;
    }

    .add-icon {
      color: #409eff;
      cursor: pointer;
      font-size: 18px;

      &:hover {
        color: #66b1ff;
      }
    }

    .recipient-tags {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .dept-tag {
        margin: 0;
      }

      .placeholder {
        color: #c0c4cc;
        font-size: 14px;
      }
    }
  }

  .top-actions {
    display: flex;
    align-items: center;
    gap: 16px;

    .action-link {
      color: #606266;
      font-size: 14px;
      cursor: pointer;

      &:hover {
        color: #409eff;
      }
    }

    .action-divider {
      width: 1px;
      height: 16px;
      background: #dcdfe6;
    }
  }
}

// 主题行
.subject-row {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #e8e8e8;

  .label {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
    width: 50px;
    white-space: nowrap;
  }

  .subject-input {
    flex: 1;

    :deep(.el-input__wrapper) {
      box-shadow: none;
      padding: 0;
    }

    :deep(.el-input__inner) {
      font-size: 14px;
    }
  }
}

// 工具栏
.toolbar {
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;

  .toolbar-row {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    gap: 8px;
    flex-wrap: wrap;

    .toolbar-group {
      display: flex;
      align-items: center;
      gap: 4px;

      &.format-group {
        margin-left: auto;
      }

      &.right-group {
        margin-left: 8px;
      }
    }

    .tool-btn {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 6px 10px;
      border: none;
      background: transparent;
      border-radius: 4px;
      cursor: pointer;
      font-size: 13px;
      color: #606266;
      transition: all 0.2s;

      &:hover {
        background: #e8e8e8;
      }

      &.active {
        background: #ecf5ff;
        color: #409eff;
      }

      .arrow {
        font-size: 12px;
      }

      .format-text {
        color: #409eff;
        font-weight: 500;
      }
    }
  }

  .format-toolbar {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    gap: 8px;
    border-top: 1px solid #e8e8e8;
    background: #f5f5f5;
    flex-wrap: wrap;

    .toolbar-group {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .tool-btn-sm {
      display: flex;
      align-items: center;
      justify-content: center;
      min-width: 28px;
      height: 28px;
      padding: 0 8px;
      border: none;
      background: transparent;
      border-radius: 4px;
      cursor: pointer;
      font-size: 13px;
      color: #606266;
      transition: all 0.2s;

      &:hover {
        background: #e0e0e0;
      }

      .arrow {
        font-size: 12px;
        margin-left: 2px;
      }

      .color-indicator {
        font-weight: bold;
      }

      .highlight-indicator {
        display: inline-block;
        width: 16px;
        height: 16px;
        background: #ffff00;
        border-radius: 2px;
      }

      .quote-icon {
        font-size: 16px;
        color: #909399;
      }

      .code-icon {
        font-family: monospace;
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

// 编辑器
.editor-container {
  flex: 1;
  padding: 20px;
  min-height: 400px;

  .editor-content {
    min-height: 350px;
    outline: none;
    font-size: 14px;
    line-height: 1.8;
    color: #303133;

    &:empty::before {
      content: '输入正文';
      color: #c0c4cc;
    }

    :deep(img) {
      max-width: 100%;
      height: auto;
    }

    :deep(table) {
      border-collapse: collapse;
      width: 100%;
      margin: 10px 0;

      td {
        border: 1px solid #ddd;
        padding: 8px;
      }
    }

    :deep(pre) {
      background: #f5f5f5;
      padding: 10px;
      border-radius: 4px;
      font-family: monospace;
      overflow-x: auto;
    }
  }
}

// 用户信息
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;

  .user-avatar {
    flex-shrink: 0;
  }

  .user-detail {
    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }

    .user-email {
      font-size: 13px;
      color: #909399;
      margin-top: 2px;
    }
  }
}
</style>
