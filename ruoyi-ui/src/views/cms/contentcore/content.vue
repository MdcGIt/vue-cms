<template>
  <div class="app-container">
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="4"
              :xs="24">
        <cms-catalog-tree 
          ref="catalogTree"
          :new-btn="true"    
          @node-click="handleTreeNodeClick">
        </cms-catalog-tree>
      </el-col>
      <el-col :span="20"
              :xs="24">
        <el-tabs v-model="activeName"
                 @tab-click="handleTabClick">
          <el-tab-pane label="内容列表"
                       name="contentList">
              <cms-content-list v-if="activeName==='contentList'" :cid="selectedCatalogId"></cms-content-list>
          </el-tab-pane>
          <el-tab-pane label="页面部件"
                       name="pageWdiget">
              <cms-pagewidget-list v-if="activeName==='pageWdiget'" :cid="selectedCatalogId"></cms-pagewidget-list>
          </el-tab-pane>
          <el-tab-pane label="回收站"
                       name="recycle">
            TODO...
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import CMSCatalogTree from '@/views/cms/contentcore/catalogTree';
import CMSContentList from '@/views/cms/contentcore/contentList';
import CMSPageWidget from '@/views/cms/contentcore/pageWidget';

export default {
  name: "CMSContent",
  components: {
    'cms-catalog-tree': CMSCatalogTree,
    'cms-content-list': CMSContentList,
    'cms-pagewidget-list': CMSPageWidget
  },
  data () {
    return {
      loading: false,
      activeName: 'contentList',
      selectedCatalogId: '',
    };
  },
  methods: {
    handleTabClick (tab, event) {
    },
    handleTreeNodeClick(data) {
      this.selectedCatalogId = data && data != null ? data.id : '';
    }
  }
};
</script>