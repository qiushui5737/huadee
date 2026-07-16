<template>
  <div class="home">
    <section class="hero">
      <div class="hero-copy"><span>遂宁政务服务</span><h1>百姓大小事　遂心办</h1><p>让数据多跑路，让群众少跑腿</p></div>
      <form class="search" @submit.prevent="onSearch"><input v-model="keyword" placeholder="请输入您要搜索的关键词"/><button aria-label="搜索"><Search/></button></form>
      <div class="hot">热门搜索：<button v-for="word in hotWords.slice(0,5)" :key="word" @click="keyword=word;onSearch()">{{ word }}</button></div>
    </section>

    <section class="carousel" @mouseenter="pause=true" @mouseleave="pause=false">
      <a v-for="(slide,index) in slides" :key="slide.title" v-show="index===current" :href="slide.link" class="slide">
        <img :src="slide.image" :alt="slide.title"/><div class="slide-caption"><small>{{ slide.category }}</small><h2>{{ slide.title }}</h2><p>{{ slide.summary }}</p></div>
      </a>
      <div class="dots"><button v-for="(_,i) in slides" :key="i" :class="{active:i===current}" @click="current=i" :aria-label="`第${i+1}张`"/></div>
    </section>

    <section class="notice-band"><div class="section-icon"><Bell/></div><b>通知公告</b><div class="ticker"><span>遂宁市政务服务中心关于优化预约办理服务的通知</span><span>2026年度惠民惠农政策申报指南发布</span></div><a href="#notices">查看更多</a></section>

    <section id="notices" class="content-section notices"><div class="section-head"><div><span>政务资讯</span><h2>通知公告</h2></div><router-link to="/search">更多公告 <ArrowRight/></router-link></div><div class="notice-grid"><article class="featured"><time>07-16</time><h3>遂宁市人民政府关于推进政务服务提质增效的通知</h3><p>持续推进政务服务标准化、规范化、便利化，为群众和企业提供高效便捷服务。</p></article><ul><li v-for="item in notices" :key="item.title"><a href="#">{{ item.title }}</a><time>{{ item.date }}</time></li></ul></div></section>

    <section class="service-section"><div class="section-head centered"><div><span>高频事项</span><h2>个人服务</h2></div></div><div class="service-grid"><router-link v-for="item in personalServices" :key="item.name" to="/service"><component :is="item.icon"/><b>{{ item.name }}</b><small>{{ item.desc }}</small></router-link></div></section>

    <section class="info-section"><div class="section-head"><div><span>便民导航</span><h2>网站信息</h2></div></div><div class="info-grid"><div><h3>政府信息公开</h3><p>政策文件、政府公报、权责清单集中查阅</p><router-link to="/disclosure">进入栏目</router-link></div><div><h3>政民互动</h3><p>领导信箱、意见征集、在线访谈统一入口</p><router-link to="/interaction">我要留言</router-link></div><div><h3>直通部门</h3><p>快速访问市级部门和区县政府网站</p><router-link to="/dept">查看部门</router-link></div></div></section>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Bell, ArrowRight, House, Reading, FirstAidKit, Briefcase, Van, Stamp, Wallet, UserFilled } from '@element-plus/icons-vue'
import { hotKeywords } from '@/api/ai'
const router=useRouter(), keyword=ref(''), hotWords=ref<string[]>(['社保查询','公积金','不动产登记','就业创业','身份证办理'])
const current=ref(0), pause=ref(false); let timer:number|undefined
const slides=[
  {category:'城市要闻',title:'建设宜居宜业和美遂宁',summary:'持续提升城市品质，让发展成果更多惠及人民群众。',image:'https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=1800&q=85',link:'#notices'},
  {category:'民生服务',title:'政务服务走进社区',summary:'高频事项就近办、一次办，让便民服务更有温度。',image:'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=1800&q=85',link:'#services'},
  {category:'政策直达',title:'惠企政策精准送达',summary:'政策集中发布、智能匹配、快速兑现，服务企业发展。',image:'https://images.unsplash.com/photo-1450101499163-c8848c66ca85?auto=format&fit=crop&w=1800&q=85',link:'#notices'}]
const notices=[{title:'关于公开征集2026年民生实事建议的公告',date:'07-15'},{title:'遂宁市公共服务事项办事指南更新说明',date:'07-12'},{title:'关于进一步做好高校毕业生就业服务的通知',date:'07-08'},{title:'住房公积金年度结息业务办理提示',date:'07-03'}]
const personalServices=[{name:'住房服务',desc:'公积金与保障住房',icon:House},{name:'教育服务',desc:'入学与教育资助',icon:Reading},{name:'医疗健康',desc:'医保与健康服务',icon:FirstAidKit},{name:'就业创业',desc:'就业登记与补贴',icon:Briefcase},{name:'交通出行',desc:'驾驶与公共交通',icon:Van},{name:'证件办理',desc:'身份证与户籍业务',icon:Stamp},{name:'社会保障',desc:'社保查询与待遇',icon:Wallet},{name:'婚育养老',desc:'全生命周期服务',icon:UserFilled}]
onMounted(()=>{hotKeywords().then((r:any)=>{if(r.data?.length)hotWords.value=r.data}).catch(()=>{});timer=window.setInterval(()=>{if(!pause.value)current.value=(current.value+1)%slides.length},4500)})
onBeforeUnmount(()=>window.clearInterval(timer));watch(current,()=>{})
function onSearch(){if(keyword.value.trim())router.push({path:'/search',query:{q:keyword.value.trim()}})}
</script>
<style scoped>
.home{background:#fff}.hero{min-height:420px;padding:75px 8% 55px;text-align:center;background:radial-gradient(circle at 50% 20%,#fff 0,#f4faff 55%,#eaf5fd 100%);position:relative}.hero-copy span{color:#0871bc;font-size:16px}.hero-copy h1{font-family:STKaiti,KaiTi,serif;font-size:46px;color:#174f7d;margin:18px 0 8px;letter-spacing:8px}.hero-copy p{color:#7d93a5}.search{height:68px;max-width:920px;margin:42px auto 15px;display:flex;background:#fff;border-radius:4px;box-shadow:0 10px 35px rgba(23,91,143,.14);overflow:hidden}.search input{flex:1;border:0;padding:0 30px;font-size:18px;outline:0}.search button{width:118px;border:0;background:#1382d1;color:#fff;cursor:pointer}.search svg{width:31px}.hot{color:#8a9aa8}.hot button{border:0;background:none;color:#5f819b;cursor:pointer}.carousel{height:420px;position:relative;overflow:hidden}.slide{position:absolute;inset:0;color:#fff;text-decoration:none}.slide img{width:100%;height:100%;object-fit:cover}.slide::after{content:'';position:absolute;inset:0;background:linear-gradient(90deg,rgba(5,28,48,.74),rgba(5,28,48,.1) 65%)}.slide-caption{position:absolute;z-index:1;left:9%;top:50%;transform:translateY(-50%);max-width:620px}.slide-caption small{padding:6px 12px;background:#1683c9}.slide-caption h2{font-size:36px;margin:20px 0 10px}.slide-caption p{font-size:17px}.dots{position:absolute;z-index:2;bottom:25px;left:50%;transform:translateX(-50%);display:flex;gap:10px}.dots button{width:32px;height:5px;border:0;background:rgba(255,255,255,.55);cursor:pointer}.dots .active{background:#fff}.notice-band{margin:0 6%;min-height:92px;padding:0 28px;display:flex;align-items:center;gap:18px;background:#edf7ff;border-bottom:1px solid #c7e6fa}.section-icon{width:48px;height:48px;border-radius:50%;background:#1587d2;color:#fff;display:grid;place-items:center}.section-icon svg{width:24px}.notice-band b{font-size:23px}.ticker{flex:1;display:flex;gap:38px;color:#3e596c}.notice-band>a{color:#718899;text-decoration:none}.content-section,.service-section,.info-section{padding:75px 8%}.section-head{display:flex;justify-content:space-between;align-items:end;margin-bottom:32px}.section-head span{color:#0d79bf}.section-head h2{font-size:30px;margin:7px 0 0}.section-head a{color:#56788e;text-decoration:none;display:flex;align-items:center;gap:5px}.section-head svg{width:18px}.notice-grid{display:grid;grid-template-columns:.8fr 1.2fr;gap:45px}.featured{padding:30px;background:#f2f7fb;border-left:4px solid #147bbd}.featured time{color:#1680c5;font-size:22px}.featured h3{font-size:23px;line-height:1.6}.featured p{color:#6e7f8c;line-height:1.8}.notice-grid ul{list-style:none;margin:0;padding:0}.notice-grid li{display:flex;justify-content:space-between;padding:18px 5px;border-bottom:1px solid #e5e9ed}.notice-grid a{color:#303b43;text-decoration:none}.notice-grid time{color:#94a2ad}.service-section{background:#f4f8fb}.centered{justify-content:center;text-align:center}.service-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:16px}.service-grid a{min-height:135px;padding:24px;background:#fff;border:1px solid #e0e8ef;text-decoration:none;color:#263b4a;display:grid;grid-template-columns:48px 1fr;grid-template-rows:1fr 1fr;align-items:center}.service-grid svg{grid-row:1/3;width:34px;color:#1680c5}.service-grid b{font-size:17px}.service-grid small{color:#8394a1}.info-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:22px}.info-grid>div{padding:32px;border-top:4px solid #1178ba;background:#f8fafc}.info-grid h3{font-size:22px}.info-grid p{color:#718391;min-height:45px}.info-grid a{color:#0874b9;text-decoration:none}@media(max-width:800px){.hero{min-height:340px;padding:50px 20px}.hero-copy h1{font-size:31px;letter-spacing:3px}.search{height:58px}.carousel{height:310px}.notice-band{margin:0;padding:18px}.ticker span+span{display:none}.content-section,.service-section,.info-section{padding:50px 20px}.notice-grid,.info-grid{grid-template-columns:1fr}.service-grid{grid-template-columns:repeat(2,1fr)}.slide-caption{left:25px}.slide-caption h2{font-size:27px}}
</style>
