<template>
  <div class="write-letter-page">
    <!-- 椤堕儴鎿嶄綔鏍?-->
    <div class="top-bar">
      <div class="recipient-row">
        <span class="label">鏀朵欢浜?/span>
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
          <span v-if="selectedDepts.length === 0" class="placeholder">璇烽€夋嫨鐩爣閮ㄩ棬</span>
        </div>
      </div>
      <div class="top-actions">
        <el-switch v-model="form.isPublic" active-text="鍏紑" inactive-text="绉佸瘑" style="margin-right: 12px;" />
        <span class="action-link">鎶勯€?/span>
        <span class="action-link">瀵嗛€?/span>
        <span class="action-divider"></span>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">鍙戦€?/el-button>
      </div>
    </div>

    <div class="page-content">
      <el-card shadow="hover" class="letter-card">
        <template #header>
          <div class="card-header">
            <el-icon><MessageSquare /></el-icon>
            <span>濉啓淇¤淇℃伅</span>
          </div>
        </template>

        <el-form ref="formRef" :model="formData" label-width="120px" :rules="formRules">
          <el-form-item label="鏉ヤ俊绫诲瀷" prop="type">
            <el-select v-model="formData.type" placeholder="璇烽€夋嫨鏉ヤ俊绫诲瀷">
              <el-option label="鎶曡瘔寤鸿" value="complaint" />
              <el-option label="鍜ㄨ姹傚姪" value="consult" />
              <el-option label="鎰忚鍙嶉" value="feedback" />
              <el-option label="鍏朵粬" value="other" />
            </el-select>
          </el-form-item>

          <el-form-item label="涓婚" prop="title">
            <el-input v-model="formData.title" placeholder="璇疯緭鍏ユ潵淇′富棰? />
          </el-form-item>

          <el-form-item label="濮撳悕" prop="name">
            <el-input v-model="formData.name" placeholder="璇疯緭鍏ユ偍鐨勫鍚? />
          </el-form-item>

          <el-form-item label="鑱旂郴鐢佃瘽" prop="phone">
            <el-input v-model="formData.phone" placeholder="璇疯緭鍏ヨ仈绯荤數璇? />
          </el-form-item>

          <el-form-item label="鐢靛瓙閭">
            <el-input v-model="formData.email" placeholder="璇疯緭鍏ョ數瀛愰偖绠憋紙閫夊～锛? />
          </el-form-item>

          <el-form-item label="鏉ヤ俊鍐呭" prop="content">
            <el-input v-model="formData.content" type="textarea" :rows="6" placeholder="璇疯缁嗘弿杩版偍鐨勮瘔姹?.." />
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button @click="handleCancel">
                <el-icon><ArrowLeft /></el-icon> 杩斿洖
              </el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                <el-icon><Send /></el-icon> 鎻愪氦鏉ヤ俊
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 宸ュ叿鏍?-->
    <div class="toolbar">
      <div class="toolbar-row">
        <div class="toolbar-group">
          <button class="tool-btn" @click="execCmd('undo')" title="鎾ら攢">
            <el-icon><RefreshLeft /></el-icon>
          </button>
          <button class="tool-btn" @click="execCmd('redo')" title="閲嶅仛">
            <el-icon><RefreshRight /></el-icon>
          </button>
        </div>
        <div class="toolbar-group">
          <button class="tool-btn" @click="insertImage" title="鍥剧墖">
            <el-icon><Picture /></el-icon>
            <span>鍥剧墖</span>
          </button>
          <el-dropdown trigger="click" @command="handleInsert">
            <button class="tool-btn">
              <el-icon><CirclePlus /></el-icon>
              <span>鎻掑叆</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="table">琛ㄦ牸</el-dropdown-item>
                <el-dropdown-item command="link">閾炬帴</el-dropdown-item>
                <el-dropdown-item command="file">闄勪欢</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <button class="tool-btn" title="瀵煎叆鏂囨。">
            <el-icon><Document /></el-icon>
            <span>瀵煎叆鏂囨。</span>
            <el-icon class="arrow"><ArrowDown /></el-icon>
          </button>
          <button class="tool-btn" title="鏃ョ▼">
            <el-icon><Calendar /></el-icon>
            <span>鏃ョ▼</span>
          </button>
          <button class="tool-btn" title="琛ㄦ儏">
            <el-icon><ChatLineRound /></el-icon>
            <span>琛ㄦ儏</span>
          </button>
        </div>
        <div class="toolbar-group format-group">
          <button class="tool-btn active" title="鏍煎紡">
            <span class="format-text">Aa 鏍煎紡</span>
            <el-icon class="arrow"><ArrowUp /></el-icon>
          </button>
        </div>
        <div class="toolbar-group right-group">
          <el-dropdown trigger="click">
            <button class="tool-btn">
              <el-icon><EditPen /></el-icon>
              <span>绛惧悕</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>榛樿绛惧悕</el-dropdown-item>
                <el-dropdown-item>绠＄悊绛惧悕</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <button class="tool-btn" title="鏇村">
            <el-icon><MoreFilled /></el-icon>
          </button>
        </div>
      </div>

      <!-- 鏍煎紡宸ュ叿鏍?-->
      <div class="format-toolbar" v-if="showFormatToolbar">
        <div class="toolbar-group">
          <button class="tool-btn-sm" title="娓呴櫎鏍煎紡">
            <el-icon><Brush /></el-icon>
          </button>
          <button class="tool-btn-sm" title="娓呴櫎鏍峰紡">
            <el-icon><Brush /></el-icon>
          </button>
        </div>
        <div class="toolbar-group">
          <el-dropdown trigger="click" @command="handleFont">
            <button class="tool-btn-sm">榛樿瀛椾綋 <el-icon class="arrow"><ArrowDown /></el-icon></button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="瀹嬩綋">瀹嬩綋</el-dropdown-item>
                <el-dropdown-item command="寰蒋闆呴粦">寰蒋闆呴粦</el-dropdown-item>
                <el-dropdown-item command="榛戜綋">榛戜綋</el-dropdown-item>
                <el-dropdown-item command="妤蜂綋">妤蜂綋</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click" @command="handleFontSize">
            <button class="tool-btn-sm">瀛楀彿 <el-icon class="arrow"><ArrowDown /></el-icon></button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="1">灏?/el-dropdown-item>
                <el-dropdown-item command="3">姝ｅ父</el-dropdown-item>
                <el-dropdown-item command="5">澶?/el-dropdown-item>
                <el-dropdown-item command="7">鐗瑰ぇ</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="toolbar-group">
          <button class="tool-btn-sm" @click="execCmd('bold')" title="鍔犵矖"><strong>B</strong></button>
          <button class="tool-btn-sm" @click="execCmd('italic')" title="鏂滀綋"><em>I</em></button>
          <button class="tool-btn-sm" @click="execCmd('underline')" title="涓嬪垝绾?><u>U</u></button>
          <button class="tool-btn-sm" @click="execCmd('strikeThrough')" title="鍒犻櫎绾?><s>S</s></button>
        </div>
        <div class="toolbar-group">
          <el-dropdown trigger="click" @command="(cmd: string) => execCmdArg('foreColor', cmd)">
            <button class="tool-btn-sm" title="瀛椾綋棰滆壊">
              <span class="color-indicator" style="border-bottom: 2px solid #f56c6c;">A</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="#000000">榛戣壊</el-dropdown-item>
                <el-dropdown-item command="#f56c6c">绾㈣壊</el-dropdown-item>
                <el-dropdown-item command="#409eff">钃濊壊</el-dropdown-item>
                <el-dropdown-item command="#67c23a">缁胯壊</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click" @command="(cmd: string) => execCmdArg('hiliteColor', cmd)">
            <button class="tool-btn-sm" title="鑳屾櫙鑹?>
              <span class="highlight-indicator"></span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="#ffff00">榛勮壊</el-dropdown-item>
                <el-dropdown-item command="#00ff00">缁胯壊</el-dropdown-item>
                <el-dropdown-item command="#00ffff">闈掕壊</el-dropdown-item>
                <el-dropdown-item command="transparent">鏃?/el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="toolbar-group">
          <button class="tool-btn-sm" @click="execCmd('insertUnorderedList')" title="鏃犲簭鍒楄〃">
            <el-icon><List /></el-icon>
          </button>
          <button class="tool-btn-sm" @click="execCmd('insertOrderedList')" title="鏈夊簭鍒楄〃">
            <el-icon><Sort /></el-icon>
          </button>
          <button class="tool-btn-sm" @click="execCmd('indent')" title="澧炲姞缂╄繘">
            <el-icon><DArrowRight /></el-icon>
          </button>
          <button class="tool-btn-sm" @click="execCmd('outdent')" title="鍑忓皯缂╄繘">
            <el-icon><DArrowLeft /></el-icon>
          </button>
        </div>
        <div class="toolbar-group">
          <button class="tool-btn-sm" @click="execCmd('justifyLeft')" title="宸﹀榻?>
            <el-icon><Menu /></el-icon>
          </button>
          <button class="tool-btn-sm" @click="execCmd('justifyCenter')" title="灞呬腑">
            <el-icon><Grid /></el-icon>
          </button>
          <button class="tool-btn-sm" @click="execCmd('justifyRight')" title="鍙冲榻?>
            <el-icon><Fold /></el-icon>
          </button>
        </div>
        <div class="toolbar-group">
          <button class="tool-btn-sm" @click="execCmd('formatBlock', 'blockquote')" title="寮曠敤">
            <span class="quote-icon">鉂?/span>
          </button>
          <button class="tool-btn-sm" @click="insertCode" title="浠ｇ爜">
            <span class="code-icon">&lt;/&gt;</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 姝ｆ枃缂栬緫鍖?-->
    <div class="editor-container">
      <div
        ref="editorRef"
        class="editor-content"
        contenteditable="true"
        @compositionstart="isComposing = true"
        @compositionend="onCompositionEnd"
        @blur="syncContent"
        placeholder="杈撳叆姝ｆ枃"
      ></div>
    </div>

    <!-- 搴曢儴鐢ㄦ埛淇℃伅 -->
    <div class="user-info">
      <div class="user-avatar">
        <el-avatar :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
      </div>
      <div class="user-detail">
        <div class="user-name">{{ userInfo.name }}</div>
        <div class="user-email">{{ userInfo.email }}</div>
      </div>
    </div>

    <!-- 閮ㄩ棬閫夋嫨寮圭獥 -->
    <el-dialog v-model="deptSelectorVisible" title="閫夋嫨鐩爣閮ㄩ棬" width="500px">
      <el-select v-model="tempDept" placeholder="璇烽€夋嫨閮ㄩ棬" style="width: 100%">
        <el-option label="鐪佹斂搴滃姙鍏巺" value="GOV" />
        <el-option label="鏁欒偛鍘? value="EDU" />
        <el-option label="鍗仴濮? value="HEA" />
        <el-option label="浣忓缓鍘? value="HOU" />
        <el-option label="浜虹ぞ鍘? value="SOC" />
        <el-option label="鍏畨鍘? value="POL" />
        <el-option label="璐㈡斂鍘? value="FIN" />
        <el-option label="浜ら€氬巺" value="TRA" />
      </el-select>
      <template #footer>
        <el-button @click="deptSelectorVisible = false">鍙栨秷</el-button>
        <el-button type="primary" @click="confirmDept">纭畾</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { MessageSquare, ArrowLeft, Send } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const showFormatToolbar = ref(true)
const deptSelectorVisible = ref(false)
const tempDept = ref('')
const isComposing = ref(false)

const formData = reactive({
  type: '',
  title: '',
  content: '',
  contactName: '',
  targetDept: '',
  type: '鍜ㄨ',
  isPublic: true
})

const formRules = {
  type: [
    { required: true, message: '璇烽€夋嫨鏉ヤ俊绫诲瀷', trigger: 'change' }
  ],
  title: [
    { required: true, message: '璇疯緭鍏ユ潵淇′富棰?, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '璇疯緭鍏ュ鍚?, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '璇疯緭鍏ヨ仈绯荤數璇?, trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '璇疯緭鍏ユ纭殑鎵嬫満鍙?, trigger: 'blur' }
  ],
  content: [
    { required: true, message: '璇疯緭鍏ユ潵淇″唴瀹?, trigger: 'blur' }
  ]
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

// 瀵屾枃鏈懡浠?
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
  const url = prompt('璇疯緭鍏ュ浘鐗嘦RL锛?)
  if (url) {
    document.execCommand('insertImage', false, url)
  }
}

const insertCode = () => {
  const code = prompt('璇疯緭鍏ヤ唬鐮侊細')
  if (code) {
    document.execCommand('insertHTML', false, `<pre style="background:#f5f5f5;padding:10px;border-radius:4px;font-family:monospace;">${code}</pre><p><br></p>`)
  }
}

const handleInsert = (command: string) => {
  if (command === 'table') {
    const html = '<table border="1" style="border-collapse:collapse;width:100%;"><tr><td style="border:1px solid #ddd;padding:8px;">鍗曞厓鏍?</td><td style="border:1px solid #ddd;padding:8px;">鍗曞厓鏍?</td></tr><tr><td style="border:1px solid #ddd;padding:8px;">鍗曞厓鏍?</td><td style="border:1px solid #ddd;padding:8px;">鍗曞厓鏍?</td></tr></table><p><br></p>'
    document.execCommand('insertHTML', false, html)
  } else if (command === 'link') {
    const url = prompt('璇疯緭鍏ラ摼鎺RL锛?)
    if (url) {
      document.execCommand('createLink', false, url)
    }
  } else if (command === 'file') {
    ElMessage.info('闄勪欢鍔熻兘寮€鍙戜腑')
  }
  editorRef.value?.focus()
}

// 杈撳叆澶勭悊锛氫粎鍦ㄥけ鐒︽椂鍚屾锛岄伩鍏嶅搷搴斿紡鏇存柊骞叉壈 contenteditable 鍏夋爣
const syncContent = () => {
  if (editorRef.value) {
    form.content = editorRef.value.innerHTML
  }
}

const onCompositionEnd = () => {
  isComposing.value = false
}

// 鎻愪氦
const handleSubmit = async () => {
  // 鍏堝悓姝ョ紪杈戝櫒鍐呭锛堢偣鍑绘寜閽椂缂栬緫鍣ㄥ彲鑳芥湭澶辩劍锛?
  syncContent()
  if (!form.title.trim()) {
    ElMessage.warning('璇疯緭鍏ヤ俊浠舵爣棰?)
    return
  }
  if (!editorRef.value || !editorRef.value.innerText.trim()) {
    ElMessage.warning('璇疯緭鍏ヤ俊浠跺唴瀹?)
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
      ElMessage.success(`淇′欢鍙戦€佹垚鍔燂紒淇′欢鍗曞彿锛?{consultNo}`)
      router.push('/interaction')
    } else {
      ElMessage.error(res.message || '鍙戦€佸け璐?)
    }
  } catch (error) {
    ElMessage.error('鎻愪氦澶辫触锛岃绋嶅悗閲嶈瘯')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.push('/interaction')
}
</script>

<style scoped>
.write-letter-page {
  min-height: calc(100vh - 100px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.page-header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 30px 40px;
  color: #fff;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-top: 16px;
  margin-bottom: 0;
}

.page-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 40px;
}

.letter-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #1890ff;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style>
