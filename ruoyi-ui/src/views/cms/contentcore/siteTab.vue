<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane label="基础信息" name="basicInfo">
        <cms-site-info v-if="this.activeName=='basicInfo'" :site="siteId"></cms-site-info>
      </el-tab-pane>
      <el-tab-pane label="扩展配置" name="extends">
        <cms-site-extend v-if="this.activeName=='extends'" :site="siteId"></cms-site-extend>
      </el-tab-pane>
      <el-tab-pane label="扩展属性" name="props">
        <cms-site-property v-if="this.activeName=='props'" :site="siteId"></cms-site-property>
      </el-tab-pane>
      <el-tab-pane label="默认模板" name="defaultTemplate">
        <cms-site-dtemplate v-if="this.activeName=='defaultTemplate'" :site="siteId"></cms-site-dtemplate>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<style scoped>
</style>
<script>
import CMSSiteInfo from '@/views/cms/contentcore/siteInfo';
import CMSSiteExtend from '@/views/cms/contentcore/siteExtend';
import CMSSiteProperty from '@/views/cms/contentcore/siteProperty';
import CMSSiteDefaultTemplate from '@/views/cms/contentcore/siteDefaultTemplate';

export default {
  name: "CMSSiteTab",
  components: {
    'cms-site-info': CMSSiteInfo,
    'cms-site-extend': CMSSiteExtend,
    "cms-site-property": CMSSiteProperty,
    "cms-site-dtemplate": CMSSiteDefaultTemplate
  },
  data () {
    return {
      activeName: 'basicInfo',
      siteId: this.$route.query.siteId,
    }
  },
  methods: {
    handleTabClick (tab, event) {
    }
  }
};
</script>