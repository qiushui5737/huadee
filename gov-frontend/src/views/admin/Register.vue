<template>
  <div class="register-page">
    <header class="header">
      <router-link to="/" class="brand"><span class="emblem">政</span><span><b>政府网站集约化平台</b><small>GOVERNMENT SERVICE PLATFORM</small></span></router-link>
      <span>已有账号？<router-link to="/admin/login">返回登录</router-link></span>
    </header>
    <main>
      <div class="steps"><div class="active">1　填写账号资料</div><div>2　确认注册信息</div><div>3　完成</div></div>
      <section class="card">
        <h1>个人注册</h1><p class="subtitle">请如实填写账号和个人资料，标有 * 的项目为必填项</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="grid">
            <el-form-item label="用户名" prop="username"><el-input v-model="form.username" placeholder="5-20位字母、数字或下划线" /></el-form-item>
            <el-form-item label="真实姓名" prop="realName"><el-input v-model="form.realName" placeholder="请输入真实姓名" /></el-form-item>
            <el-form-item label="性别" prop="gender"><el-select v-model="form.gender" placeholder="请选择性别" style="width:100%"><el-option label="男" value="男"/><el-option label="女" value="女"/><el-option label="其他" value="其他"/></el-select></el-form-item>
            <el-form-item label="身份证号" prop="idCard"><el-input v-model="form.idCard" maxlength="18" placeholder="请输入18位身份证号" /></el-form-item>
            <el-form-item label="手机号码（仅作联系信息）" prop="phone"><el-input v-model="form.phone" maxlength="11" placeholder="无需短信验证" /></el-form-item>
            <el-form-item label="电子邮箱" prop="email"><el-input v-model="form.email" placeholder="请输入电子邮箱" /></el-form-item>
            <el-form-item label="所属部门" prop="deptCode"><el-select v-model="form.deptCode" filterable allow-create placeholder="请选择或输入部门" style="width:100%"><el-option label="平台管理部门" value="ADMIN"/><el-option label="教育部门" value="EDU"/><el-option label="卫生健康部门" value="HEA"/><el-option label="其他部门" value="OTHER"/></el-select></el-form-item>
            <el-form-item label="联系地址" prop="address"><el-input v-model="form.address" placeholder="请输入联系地址" /></el-form-item>
            <el-form-item label="设置密码" prop="password"><el-input v-model="form.password" type="password" show-password placeholder="至少8位，包含字母和数字" /></el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" @keyup.enter="submit" /></el-form-item>
          </div>
          <el-checkbox v-model="agreed">我已阅读并同意《平台注册协议》和《隐私保护政策》</el-checkbox>
          <el-button class="submit" type="primary" :loading="loading" @click="submit">注 册</el-button>
        </el-form>
      </section>
    </main>
    <footer>主办单位：政府网站集约化平台　｜　请如实填报注册信息</footer>
  </div>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { adminRegister, type RegisterData } from '@/api/admin'
const router = useRouter(); const formRef = ref<FormInstance>(); const loading = ref(false); const agreed = ref(false)
const form = reactive<RegisterData>({ username:'', password:'', confirmPassword:'', realName:'', gender:'', idCard:'', phone:'', email:'', deptCode:'', address:'' })
const validatePassword = (_r:any, value:string, callback:any) => /^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$/.test(value) ? callback() : callback(new Error('密码至少8位，且必须包含字母和数字'))
const validateConfirm = (_r:any, value:string, callback:any) => value === form.password ? callback() : callback(new Error('两次输入的密码不一致'))
const rules: FormRules = {
  username:[{required:true,message:'请输入用户名',trigger:'blur'},{pattern:/^[A-Za-z][A-Za-z0-9_]{4,19}$/,message:'须为5-20位且以字母开头',trigger:'blur'}],
  realName:[{required:true,message:'请输入真实姓名',trigger:'blur'}], gender:[{required:true,message:'请选择性别',trigger:'change'}],
  idCard:[{required:true,message:'请输入身份证号',trigger:'blur'},{pattern:/^[0-9]{17}[0-9Xx]$/,message:'身份证号格式不正确',trigger:'blur'}],
  phone:[{required:true,message:'请输入手机号',trigger:'blur'},{pattern:/^1[0-9]{10}$/,message:'手机号格式不正确',trigger:'blur'}],
  email:[{required:true,message:'请输入邮箱',trigger:'blur'},{type:'email',message:'邮箱格式不正确',trigger:'blur'}],
  deptCode:[{required:true,message:'请选择所属部门',trigger:'change'}], address:[{required:true,message:'请输入联系地址',trigger:'blur'}],
  password:[{required:true,message:'请输入密码',trigger:'blur'},{validator:validatePassword,trigger:'blur'}], confirmPassword:[{required:true,message:'请确认密码',trigger:'blur'},{validator:validateConfirm,trigger:'blur'}]
}
async function submit(){
  if (!formRef.value || !await formRef.value.validate().catch(()=>false)) return
  if (!agreed.value) return ElMessage.warning('请先阅读并同意注册协议')
  loading.value=true
  try { await adminRegister(form); ElMessage.success('注册成功，请使用新账号登录'); router.replace('/admin/login') } finally { loading.value=false }
}
</script>
<style scoped>
.register-page{min-height:100vh;background:#f7f9fb;color:#333;display:flex;flex-direction:column}.header{height:100px;background:white;border-bottom:2px solid #1761a3;display:flex;align-items:center;justify-content:space-between;padding:0 max(7vw,40px)}.brand{display:flex;align-items:center;gap:14px;color:#b51f24;text-decoration:none}.emblem{width:56px;height:56px;border-radius:50%;background:#c9141b;color:#ffd560;border:4px solid #f2c943;display:grid;place-items:center;font-weight:bold}.brand b{font-size:26px;letter-spacing:2px}.brand small{display:block;color:#888;font-size:10px;margin-top:5px}.header a{color:#0965aa;text-decoration:none}main{width:min(1120px,92%);margin:38px auto;flex:1}.steps{display:grid;grid-template-columns:repeat(3,1fr);height:54px;margin-bottom:24px}.steps div{background:#d8dadd;display:grid;place-items:center;font-size:17px;border-right:2px solid white}.steps .active{background:#0865a9;color:white}.card{background:#fff;border:1px solid #dce1e6;padding:34px 62px 44px}.card h1{color:#075d9e;margin:0;border-bottom:2px solid #1b6bac;padding-bottom:14px}.subtitle{color:#888;margin:16px 0 26px}.grid{display:grid;grid-template-columns:1fr 1fr;gap:0 38px}.submit{display:block;width:60%;height:48px;margin:34px auto 0;font-size:18px;background:#0862a5}footer{height:70px;border-top:1px solid #ddd;background:white;display:grid;place-items:center;color:#777}@media(max-width:700px){.header{padding:0 18px}.brand b{font-size:18px}.header>span{display:none}.steps{font-size:13px}.card{padding:26px 22px}.grid{grid-template-columns:1fr}.submit{width:100%}}
</style>
