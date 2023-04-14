<template>
  <div class="app-container">
    <el-dialog :title="title"
               :visible.sync="visible"
               width="1100px"
               :close-on-click-modal="false"
               custom-class="content-selector-dialog"
               append-to-body>
      <el-container>
        <el-aside>
          <cms-catalog-tree 
            ref="catalogTree"
            @node-click="handleTreeNodeClick">
          </cms-catalog-tree>
        </el-aside>
        <el-main>
          <el-form :model="queryParams"
              ref="queryForm"
              :inline="true"
              size="small"
              class="el-form-search mb12">
            <el-form-item label="" prop="query">
              <el-input v-model="queryParams.query" placeholder="输入标题查询">
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button-group>
                <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
                <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
              </el-button-group>
            </el-form-item>
          </el-form>
          <el-table 
            v-loading="loading"
            ref="tableContentList"
            :data="contentList"
            highlight-current-row
            @row-click="handleRowClick"
            @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column label="标题" align="left" prop="title">
              <template slot-scope="scope">
                <span><i v-if="scope.row.topFlag>0" class="el-icon-top top-icon" title="置顶"></i> {{ scope.row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column label="发布时间" align="center" prop="publishDate" width="160">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.publishDate) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <pagination 
            v-show="total>0"
            :total="total"
            :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize"
            @pagination="loadContentList" />
        </el-main>
      </el-container>
      
      <div slot="footer"
            class="dialog-footer">
        <el-button type="primary" 
                    @click="handleOk">确 定</el-button>
        <el-button @click="handleClose">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style>
.content-selector-dialog .el-dialog__body {
  padding-top: 10px;
  padding-bottom: 10px;
}
.content-selector-dialog .el-aside {
  padding: 10px;
  background-color: #fff;
}
.content-selector-dialog .el-main {
  padding: 10px;
}
</style>
<script>
import { getContentList } from "@/api/contentcore/content";
import CMSCatalogTree from '@/views/cms/contentcore/catalogTree';

export default {
  name: "CMSContentSelector",
  components: {
    'cms-catalog-tree': CMSCatalogTree,
  },
  props: {
    title: {
      type: String,
      default: "选择内容",
      required: false
    },
    open: {
      type: Boolean,
      default: false,
      required: true
    }
  },
  watch: {
    open () {
      this.visible = this.open;
    },
    visible (newVal) {
      if (!newVal) {
        this.handleClose();
      } else {
        this.loadContentList();
      }
    }
  },
  data () {
    return {
      loading: false,
      visible: this.open,
      selectedContents: [],
      contentList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        status: 30,
        catalogId: '',
        query: ''
      }
    };
  },
  methods: {
    handleTreeNodeClick(data) {
      this.queryParams.catalogId = data && data != null ? data.id : ''; 
      this.loadContentList();
    },
    loadContentList () {
      if (!this.visible) {
        return;
      }
      this.loading = true;
      getContentList(this.queryParams).then(response => {
        if (response.code == 200) {
          this.contentList = response.data.rows;
          this.total = parseInt(response.data.total);
        }
        this.selectedContents = [];
        this.loading = false;
      });
    },
    handleRowClick (row) {
      this.selectedContents.forEach(row => {
          this.$refs.tableContentList.toggleRowSelection(row, false);
      });
      this.selectedContents = [];
      this.$refs.tableContentList.toggleRowSelection(row);
    },
    handleSelectionChange (selection) {
      this.selectedContents = selection;
    },
    handleOk () {
      this.$emit("ok", this.selectedContents);
    },
    // 取消按钮
    handleClose () {
      this.$emit("close");
      this.resetForm("queryForm");
      this.selectedContents = [];
      this.contentList = [];
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.loadContentList();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    }
  }
};
</script>