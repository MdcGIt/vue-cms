<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane label="TAG词" name="tagWord">
        <cms-tag-word v-if="this.activeName=='tagWord'"></cms-tag-word>
      </el-tab-pane>
      <el-tab-pane label="热词" name="hotWord">
        <cms-hot-word v-if="this.activeName=='hotWord'"></cms-hot-word>
      </el-tab-pane>
      <el-tab-pane label="敏感词" name="sensitiveWord">
        <cms-sensitive-word v-if="this.activeName=='sensitiveWord'"></cms-sensitive-word>
      </el-tab-pane>
      <el-tab-pane label="易错词" name="errorProneWord">
        <cms-errorprone-word v-if="this.activeName=='errorProneWord'"></cms-errorprone-word>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<style scoped>
</style>
<script>
import CMSTagWord from '@/views/cms/word/tagWord';
import CMSHotWord from '@/views/cms/word/hotWord';
import CMSSensitiveWord from '@/views/cms/word/sensitiveWord';
import CMSErrorProneWord from '@/views/cms/word/errorProneWord';

export default {
  name: "CMSWordTab",
  components: {
    'cms-tag-word': CMSTagWord,
    'cms-hot-word': CMSHotWord,
    'cms-sensitive-word': CMSSensitiveWord,
    'cms-errorprone-word': CMSErrorProneWord
  },
  data () {
    return {
      // 遮罩层
      loading: false,
      activeName: 'tagWord',
      siteId: this.$cache.local.get("CurrentSite"),
    };
  },
  methods: {
    handleTabClick (tab, event) {
    }
  }
};
</script>