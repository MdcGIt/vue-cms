<template>
  <div class="app-container">
    <el-dialog :title="title"
               :visible.sync="visible"
               width="800px"
               :close-on-click-modal="false"
               custom-class="content-selector-dialog"
               append-to-body>
      <div style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
        <i class="el-icon-info mr5"></i>内容将会排在下方列表选中的内容之前，选中内容置顶则排序内容也会添加置顶状态，反之则会取消置顶状态。
      </div>
      <el-form 
        :model="queryParams"
        ref="queryForm"
        :inline="true"
        size="small"
        class="el-form-search mt10 mb10"
        style="text-align:left">
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
        :data="contentList"
        highlight-current-row
        @current-change="handleSelectionChange">
        <el-table-column type="index" label="序号" align="center" width="50" />
        <el-table-column label="标题" align="left" prop="title">
          <template slot-scope="scope">
            <span><i v-if="scope.row.topFlag>0" class="el-icon-top top-icon" title="置顶"></i> {{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.CMSContentStatus" :value="scope.row.status"/>
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
      <div slot="footer" class="dialog-footer">
        <el-button type="primary"  @click="handleOk">确 定</el-button>
        <el-button @click="handleClose">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style>
.content-selector-dialog .top-icon {
  font-weight: bold;
  font-size: 12px;
  color:green;
}
</style>
<script>
import { getContentList } from "@/api/contentcore/content";

export default {
  name: "CMSContentSortDialog",
  dicts: ['CMSContentStatus'],
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
    },
    cid: {
      type: String,
      default: undefined,
      required: false
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
    },
    cid () {
      this.queryParams.catalogId = this.cid;
    }
  },
  computed: {
    okBtnDisabled() {
      return this.selectedTemplate == undefined;
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
        catalogId: this.cid,
        query: undefined
      }
    };
  },
  methods: {
    loadContentList () {
      if (!this.visible) {
        return;
      }
      this.loading = true;
      getContentList(this.queryParams).then(response => {
        this.contentList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.selectedContents = [];
        this.loading = false;
      });
    },
    handleSelectionChange (selection) {
      if (selection && selection.contentId) {
        this.selectedContents = [ selection ];
      }
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