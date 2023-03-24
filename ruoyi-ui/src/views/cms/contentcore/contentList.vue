<template>
  <div class="cms-content-list">
    <el-form :model="queryParams"
              ref="queryForm"
              size="small"
              :inline="true">
      <el-form-item prop="title">
        <el-input v-model="queryParams.title"
                  placeholder="请输入内容标题"
                  clearable
                  style="width: 200px"
                  @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item prop="contentType">
        <el-select v-model="queryParams.contentType"
                    placeholder="内容类型"
                    clearable
                    style="width: 110px">
          <el-option v-for="ct in contentTypeOptions"
                      :key="ct.id"
                      :label="ct.name"
                      :value="ct.id" />
        </el-select>
      </el-form-item>
      <el-form-item prop="status">
        <el-select v-model="queryParams.status"
                    placeholder="状态"
                    clearable
                    style="width: 110px">
          <el-option v-for="dict in dict.type.CMSContentStatus"
                      :key="dict.value"
                      :label="dict.label"
                      :value="dict.value" />
        </el-select>
        
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRange"
                        size="small"
                        style="width: 240px"
                        value-format="yyyy-MM-dd"
                        type="daterange"
                        range-separator="-"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-select @change="loadContentList"
                   v-model="queryParams.sorts"
                   size="small"
                   style="width: 140px">
          <el-option value="" label="默认排序"></el-option>
          <el-option value="[{'column':'create_time','direction':'ASC'}]" label="添加时间升序"></el-option>
          <el-option value="[{'column':'create_time','direction':'DESC'}]" label="添加时间降序"></el-option>
          <el-option value="[{'column':'publish_date','direction':'ASC'}]" label="发布时间升序"></el-option>
          <el-option value="[{'column':'publish_date','direction':'DESC'}]" label="发布时间降序"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary"
                      icon="el-icon-search"
                      @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh"
                      @click="resetQuery">重置</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>

    <el-row :gutter="10"
            class="mb8">
      <el-col :span="1.5">
        <el-popover placement="bottom-start"
                    :width="304"
                    trigger="click">
          <el-row style="margin-bottom:20px">
            <el-radio-group v-model="addContentType">
              <el-radio-button v-for="ct in contentTypeOptions"
                                :key="ct.id"
                                :label="ct.id">{{ct.name}}</el-radio-button>
            </el-radio-group>
          </el-row>
          <el-row style="text-align:right;">
            <el-button type="primary"
                        size="mini"
                        plain
                        @click="handleAdd">确认</el-button>
          </el-row>
          <el-button type="primary"
                      slot="reference"
                      icon="el-icon-plus"
                      size="mini"
                      plain
                      v-hasPermi="['contentcore:content:add']">{{ $t("Common.Add") }}
            <i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
        </el-popover>
      </el-col>
      <el-col :span="1.5">
        <el-button plain
                    type="danger"
                    icon="el-icon-delete"
                    size="mini"
                    :disabled="multiple"
                    @click="handleDelete"
                    v-hasPermi="['contentcore:content:delete']">{{ $t("Common.Delete") }}
        </el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading"
              ref="tableContentList"
              :data="contentList"
              :height="tableHeight"
              :max-height="tableMaxHeight"
              @row-click="handleRowClick"
              @selection-change="handleSelectionChange">
      <el-table-column type="selection"
                        width="50"
                        align="center" />
      <el-table-column label="标题" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-link type="primary"
                    @click="handleEdit(scope.row)"
                    class="link-type">
            <span>{{ scope.row.title }}</span>
          </el-link>
          <span class="content_attr" v-if="scope.row.topFlag>0" title="置顶">[<svg-icon icon-class="top" />]</span>
          <span 
            v-for="dict in dict.type.CMSContentAttribute"
            :key="dict.value"
            :title="dict.label">
            <span class="content_attr" v-if="scope.row.attributes.indexOf(dict.value)>-1">[<svg-icon :icon-class="dict.value" />]</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="类型"
                        width="110"
                        align="center"
                        prop="contentType"
                        :formatter="contentTypeFormat" />
      <el-table-column label="状态"
                        width="110"
                        align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusFormat(scope.row, 'status') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间"
                        align="center"
                        prop="createTime"
                        width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作"
                        align="center"
                        width="300"
                        class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini"
                      type="text"
                      icon="el-icon-s-promotion"
                      @click="handlePublish(scope.row)"
                      v-hasPermi="['contentcore:content:edit']">发布</el-button>
          <el-button size="mini"
                      type="text"
                      icon="el-icon-view"
                      @click="handlePreview(scope.row)"
                      v-hasPermi="['contentcore:content:edit']">预览</el-button>
          <el-button size="mini"
                      type="text"
                      icon="el-icon-sort"
                      @click="handleSort(scope.row)"
                      v-hasPermi="['contentcore:content:edit']">排序</el-button>
          <el-button size="mini"
                      type="text"
                      icon="el-icon-download"
                      @click="handleOffline(scope.row)"
                      v-hasPermi="['contentcore:content:edit']">下线</el-button>
          <el-dropdown>
            <el-link :underline="false" class="row-more-btn" icon="el-icon-more"></el-link>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item icon="el-icon-edit" @click.native="handleEdit(scope.row)">编辑</el-dropdown-item>
              <el-dropdown-item icon="el-icon-delete" @click.native="handleDelete(scope.row)">删除</el-dropdown-item>
              <el-dropdown-item v-show="scope.row.topFlag<=0" icon="el-icon-top" @click.native="handleSetTop(scope.row)">置顶</el-dropdown-item>
              <el-dropdown-item v-show="scope.row.topFlag>0" icon="el-icon-bottom" @click.native="handleCancelTop(scope.row)">取消置顶</el-dropdown-item>
              <el-dropdown-item icon="el-icon-document-copy" @click.native="handleCopy(scope.row)">复制</el-dropdown-item>
              <el-dropdown-item icon="el-icon-right" @click.native="handleMove(scope.row)">转移</el-dropdown-item>
              <el-dropdown-item icon="el-icon-document" @click.native="handleArchive(scope.row)">归档</el-dropdown-item>
              <el-dropdown-item icon="el-icon-search" @click.native="handleCreateIndex(scope.row)">生成索引</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0"
                :total="total"
                :page.sync="queryParams.pageNum"
                :limit.sync="queryParams.pageSize"
                @pagination="loadContentList" />
              
    <!-- 置顶时间设置弹窗 -->  
    <el-dialog title="置顶"
               width="400px"
               :visible.sync="topDialogVisible"
               :close-on-click-modal="false"
               append-to-body>
      <el-form ref="top_form"
               label-width="100px"
               :model="topForm">
        <el-form-item label="置顶结束时间" prop="topEndTime">
          <el-date-picker v-model="topForm.topEndTime" 
                          :picker-options="topEndTimePickerOptions"
                          type="datetime">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" 
                    @click="handleTopDialogOk">确 定</el-button>
        <el-button @click="this.topDialogVisible=false">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress title="发布任务" :open.sync="openProgress" :taskId="taskId"></cms-progress>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open="openCatalogSelector"
      :showCopyToolbar="isCopy"
      :multiple="isCopy"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容排序组件 -->
    <cms-content-sort
      :open="openContentSortDialog"
      :cid="catalogId"
      @ok="handleContentSortDialogOk"
      @close="handleContentSortDialogClose"></cms-content-sort>
  </div>
</template>
<script>
import { getContentTypes } from "@/api/contentcore/catalog";
import { 
  getContentList, delContent, publishContent, createIndexes, 
  copyContent, moveContent, setTopContent, cancelTopContent, sortContent, 
  offlineContent, archiveContent 
} from "@/api/contentcore/content";
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CMSContentSort from "@/views/cms/contentcore/contentSortDialog";
import CMSProgress from '@/views/components/Progress';

export default {
  name: "CMSContentList",
  components: { 
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-content-sort': CMSContentSort,
    'cms-progress': CMSProgress
  },
  dicts: ['CMSContentStatus', 'CMSContentAttribute'],
  props: {
    cid: {
      type: String,
      default: undefined,
      required: false,
    }
  },
  data () {
    return {
      // 遮罩层
      loading: false,
      contentTypeOptions: [],
      catalogId: this.cid,
      contentList: null,
      total: 0,
      tableHeight: 600, // 表格高度
      tableMaxHeight: 600, // 表格最大高度
      selectedRows: [], // 表格选中行
      single: true,
      multiple: true,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        contentType: undefined,
        status: undefined,
        catalogId: undefined,
        beginTime: undefined,
        endTime: undefined,
        sorts: ''
      },
      topDialogVisible: false,
      topForm: {
        topEndTime: undefined
      },
      topEndTimePickerOptions: {
        disabledDate(time) {
            return time.getTime() < Date.now() - 8.64e7;//如果没有后面的-8.64e7就是不可以选择今天的 
         }
      },
      openProgress: false,
      taskId: "",
      addContentType: "",
      openCatalogSelector: false, // 栏目选择弹窗
      isCopy: false,
      openContentSortDialog: false // 内容选择弹窗
    };
  },
  watch: {
    cid(newVal) { 
      this.catalogId = newVal;
    },
    catalogId(newVal) {
      this.loadContentList();
    },
  },
  created () {
    this.changeTableHeight();
    getContentTypes().then(response => {
      this.contentTypeOptions = response.data;
      this.addContentType = this.contentTypeOptions[0].id;
    });
    if (this.catalogId && this.catalogId > 0) {
      this.loadContentList();
    }
  },
  methods: {
    loadContentList () {
      this.loading = true;
      this.queryParams.catalogId = this.catalogId;
      getContentList(this.queryParams).then(response => {
        this.contentList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    statusFormat (row, column) {
      return this.selectDictLabel(this.dict.type.CMSContentStatus, row[column]);
    },
    contentTypeFormat (row, column) {
      var hitValue = [];
      this.contentTypeOptions.forEach(ct => {
        if (ct.id == ('' + row.contentType)) {
          hitValue = ct.name;
          return;
        }
      });
      return hitValue;
    },
    handleSelectionChange (selection) {
      this.selectedRows = selection;
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleRowClick (currentRow) {
      this.toggleAllCheckedRows();
      this.$refs.tableContentList.toggleRowSelection(currentRow);
      this.selectedRows.push(currentRow);
    },
    toggleAllCheckedRows() {
      this.selectedRows.forEach(row => {
          this.$refs.tableContentList.toggleRowSelection(row, false);
      });
      this.selectedRows = [];
    },
    statusTagType(status) {
      if (status == 40) {
        return "warning";
      } else if (status == 20 || status == 30) {
        return "success";
      } else if (status == 0) {
        return "info";
      }
      return "";
    },
    handleQuery () {
      this.queryParams.page = 1;
      this.loadContentList();
    },
    resetQuery () {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleAdd () {
      if (!this.catalogId) {
        this.$modal.msgError("请先选择一个栏目");
        return;
      }
      this.openEditor(this.catalogId, 0, this.addContentType);
    },
    handleEdit (row) {
      this.openEditor(row.catalogId, row.contentId, row.contentType);
    },
    openEditor(catalogId, contentId, contentType) {
      this.$router.push({ path: "/cms/content/editor", query: { type: contentType, catalogId: catalogId, id: contentId } });
    },
    handleDelete (row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(row => row.contentId);
      this.$modal.confirm("是否确认删除?").then(function () {
        return delContent(contentIds);
      }).then(() => {
        this.loadContentList();
        this.$modal.msgSuccess("删除成功");
      }).catch(function () { });
    },
    handlePublish (row) {
      const contentIds = row ? [ row.contentId ] : this.selectedRows.map(item => { return item.contentId });
      if (contentIds.length == 0) {
        this.$modal.msgWarn("请先选择一条记录");
        return;
      }
      this.$modal.loading("正在发布...");
      publishContent(contentIds).then(response => {
        this.$modal.closeLoading();
        if (response.code == 200) {
          this.loadContentList();
        } else {
          this.$modal.msgError(response.msg);
        }
      });
    },
    handlePreview (row) {
      let contentId = undefined;
      if (row) {
        contentId = row.contentId;
      } else if (this.selectedRows.length > 0) {
        contentId = this.selectedRows[0].contentId;
      } else {
        this.$modal.msgWarn("请先选择一条记录");
        return;
      }
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "content", dataId: contentId },
      });
      window.open(routeData.href, '_blank');
    },
    handleDropdownBtn (command, row) {
    },
    changeTableHeight () {
      let height = document.body.offsetHeight // 网页可视区域高度
      this.tableHeight = height - 330;
      this.tableMaxHeight = this.tableHeight;
    },
    handleCreateIndex(row) {
      createIndexes(row.contentId).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess("操作成功");
        } else {
          this.$modal.msgError(response.msg);
        }
      });
    },
    handleCopy(row) {
      if (row.contentId) {
        this.selectedRows = [ row ];
      }
      this.isCopy = true;
      this.openCatalogSelector = true;
    },
    doCopy(catalogs, copyType) {
      const data = {  
        contentIds: this.selectedRows.map(item => item.contentId),
        catalogIds: catalogs.map(item => item.id),
        copyType: copyType
      };
      copyContent(data).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess("操作成功");
          this.openCatalogSelector = false;
        }
      });
    },
    handleMove(row) {
      if (row.contentId) {
        this.selectedRows = [ row ];
      }
      this.isCopy = false;
      this.openCatalogSelector = true;
    },
    doMove (catalogs) {
      const data = {
        contentIds: this.selectedRows.map(item => item.contentId),
        catalogId: catalogs[0].id
      };
      moveContent(data).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess("操作成功");
          this.openCatalogSelector = false;
          this.loadContentList();
        }
      });
    },
    handleCatalogSelectorOk(catalogs, copyType) {
      if (this.isCopy) {
        this.doCopy(catalogs, copyType);
      } else {
        this.doMove(catalogs);
      }
    },
    handleCatalogSelectorClose() {
      this.openCatalogSelector = false; 
    },
    handleSetTop(row) {
      if (row.contentId) {
        this.toggleAllCheckedRows();
        this.selectedRows.push(row);
      }
      this.topDialogVisible = true;
    },
    handleTopDialogOk() {
      const contentIds = this.selectedRows.map(item => item.contentId);
      if (contentIds.length == 0) {
        this.$modal.msgWarn("请先选择一条记录");
        return;
      }
      this.$refs["top_form"].validate(valid => {
        if (valid) {
          setTopContent({ contentIds: contentIds, topEndTime: this.topForm.topEndTime }).then(response => {
            this.$modal.msgSuccess(response.msg);
            this.topDialogVisible = false;
            this.topForm.topEndTime = undefined;
            this.loadContentList();
          });
        }
      });
    },
    handleCancelTop(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      cancelTopContent(contentIds).then(response => {
        this.$modal.msgSuccess(response.msg);
        this.loadContentList();
      });
    },
    handleSort(row) {
      if (row.contentId) {
        this.toggleAllCheckedRows();
        this.selectedRows.push(row);
      }
      this.openContentSortDialog = true;
    },
    handleContentSortDialogOk(contents) {
      if (contents && contents.length > 0) {
        this.doSort(contents[0].contentId);
      }
    },
    handleContentSortDialogClose() {
      this.openContentSortDialog = false;
    },
    doSort(targetContentId) {
      const data = { contentId: this.selectedRows[0].contentId, targetContentId: targetContentId };
      sortContent(data).then(response => {
        this.$modal.msgSuccess(response.msg);
        this.openContentSortDialog = false;
        this.loadContentList();
      });
    },
    handleOffline(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      offlineContent(contentIds).then(response => {
        this.$modal.msgSuccess(response.msg);
        this.loadContentList();
      });
    },
    handleArchive(row) {
      const contentIds = row.contentId ? [ row.contentId ] : this.selectedRows.map(item => item.contentId);
      archiveContent(contentIds).then(response => {
        this.$modal.msgSuccess(response.msg);
        this.loadContentList();
      });
    }
  }
};
</script>
<style scoped>
.head-container .el-select .el-input {
  width: 110px;
}
.el-divider {
  margin-top: 10px;
}
.el-tabs__header {
  margin-bottom: 10px;
}
.pagination-container {
  height: 30px;
}
.row-more-btn {
  padding-left: 10px;
}
.top-icon {
  font-weight: bold;
  font-size: 12px;
  color:green;
}
.content_attr {
  margin-left: 2px;
}
</style>