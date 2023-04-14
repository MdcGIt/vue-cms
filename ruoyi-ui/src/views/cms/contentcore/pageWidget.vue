<template>
  <div class="pagewidget-container">
    <el-row class="mb12">
      <el-col :span="8">
        <el-button 
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd">新建</el-button>
      </el-col>
      <el-col :span="16" style="text-align: right;">
        <el-select 
          v-model="queryParams.type" size="mini"
          placeholder="选择类型"
          clearable
          @change="loadPageWidgetList">
          <el-option 
            v-for="item in pageWidgetTypes"
            :key="item.id"
            :label="item.name"
            :value="item.id" />
        </el-select>
      </el-col>
    </el-row>
    <el-row>
      <div v-loading="loading">
        <el-card shadow="hover" v-for="pageWidget in pageWidgetList" :key="pageWidget.pageWidgetId">
          <div class="item-row1">
            <el-link type="primary"
                    :underline="false"
                    @click="handleEdit(pageWidget)">
              <span class="item-catalog" v-if="pageWidget.catalogName&&pageWidget.catalogName!==null">[ {{ pageWidget.catalogName }} ]</span>
              <span class="item-name">{{ pageWidget.name }}</span>
            </el-link>
          </div>
          <div class="item-row2">
            <el-descriptions class="margin-top" :column="4">
              <el-descriptions-item label="类型">{{ pageWidgetTypeName(pageWidget.type) }}</el-descriptions-item>
              <el-descriptions-item label="编码">{{ pageWidget.code }}</el-descriptions-item>
              <el-descriptions-item label="发布通道">{{ publishPipeName(pageWidget.publishPipeCode) }}</el-descriptions-item>
              <el-descriptions-item label="发布目录">{{ pageWidget.path }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div class="item-row3">
            <el-row>
              <el-col :span="4" class="item-state">
                <dict-tag :options="dict.type.CMSPageWidgetStatus" :value="pageWidget.state"/>
              </el-col>
              <el-col :span="20" class="item-toolbar">
                <el-button size="mini"
                        type="text"
                        icon="el-icon-s-promotion"
                        @click="handlePublish(pageWidget)">发布</el-button>
                <el-button size="mini"
                          type="text"
                          icon="el-icon-view"
                          @click="handlePreview(pageWidget)">预览</el-button>
                <el-button size="mini"
                          type="text"
                          icon="el-icon-edit"
                          @click="handleEdit(pageWidget)">编辑</el-button>
                <el-button size="mini"
                          type="text"
                          icon="el-icon-delete"
                          @click="handleDelete(pageWidget)">删除</el-button>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadPageWidgetList"
      />
    </el-row>
    <!-- 添加对话框 -->
    <el-dialog title="新建页面部件"
               :visible.sync="dialogVisible"
               :close-on-click-modal="false"
               width="600px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="80px">
        <el-form-item label="类型"
                      prop="type">
          <el-select v-model="form.type"
                     placeholder="类型"
                     clearable>
            <el-option v-for="item in pageWidgetTypes"
                       :key="item.id"
                       :label="item.name"
                       :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"
                      prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编码"
                      prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="发布通道"
                      prop="publishPipeCode">
          <el-select v-model="form.publishPipeCode">
            <el-option
              v-for="pp in publishPipes"
              :key="pp.pipeCode"
              :label="pp.pipeName"
              :value="pp.pipeCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发布目录"
                      prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="备注"
                      prop="remark">
          <el-input v-model="form.remark"
                    type="textarea"
                    maxlength="100" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary"
                   @click="handleAddDialogOk">确 定</el-button>
        <el-button @click="handleAddDialogClose">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { listPageWidgetTypes, listPageWidgets, addPageWidget, deletePageWidget, publishPageWidgets } from "@/api/contentcore/pagewidget";

export default {
  name: "CMSPageWdiget",
  dicts: [ 'CMSPageWidgetStatus' ],
  props: {
    cid: {
      type: String,
      default: undefined,
      required: false,
    }
  },
  data () {
    return {
      loading: false,
      catalogId: this.cid,
      dialogVisible: false,
      publishPipes: [],
      pageWidgetTypes: [],
      pageWidgetList: [],
      total: 0,
      queryParams: {
        type: undefined,
        pageSize: 20,
        pageNo: 1
      },
      form: {
        path: 'include/pagewidget/'
      },
      rules: {
        type: [
          { required: true, message: "类型不能为空", trigger: "blur" }
        ],
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]*$", message: "不能为空且只能使用字母、数字和下划线", trigger: "blur" }
        ],
        publishPipeCode: [
          { required: true, message: "发布通道不能为空", trigger: "blur" }
        ],
        path: [
          { required: true, pattern: "^[A-Za-z0-9_\/]*$", message: "不能为空且只能使用字母、数字和下划线", trigger: "blur" }
        ]
      }
    };
  },
  watch: {
    cid(newVal) { 
      this.catalogId = newVal;
    },
    catalogId(newVal) {
      this.loadPageWidgetList();
    },
  },
  created() {
    this.loadPageWidgetList();
    this.loadPageWdigetTypes();
    this.loadPublishPipes();
  },
  methods: {
    stateTagType(state) {
      return state == 20 ? "warning" : state == 30 ? "success" : "";
    },
    loadPageWdigetTypes() {
      listPageWidgetTypes().then(response => {
        if (response.code == 200) {
          this.pageWidgetTypes = response.data.rows;
        }
      });
    },
    pageWidgetTypeName(type) {
      let pt = this.pageWidgetTypes.find(v => v.id == type);
      return pt ? pt.name : type;
    },
    loadPublishPipes() {
      getPublishPipeSelectData().then(response => {
        this.publishPipes = response.data.rows;
      });
    },
    publishPipeName(code) {
      let pp = this.publishPipes.find(v => v.pipeCode == code);
      return pp ? pp.pipeName : code
    },
    loadPageWidgetList () {
      this.loading = true;
      this.queryParams.catalogId = this.catalogId;
      listPageWidgets(this.queryParams).then(response => {
        if (response.code == 200) {
          this.pageWidgetList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      });
    },
    handleAdd() {
      this.dialogVisible = true;
      this.form = { path: 'include/pagewidget/' };
    },
    handleAddDialogClose() {
      this.dialogVisible = false;
    },
    handleAddDialogOk () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.catalogId = this.catalogId;
          addPageWidget(this.form).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess(response.msg);
              this.loadPageWidgetList();
              this.handleAddDialogClose();
            }
          });
        }
      });
    },
    handleDelete(row) {
      const pageWidgetIds = [ row.pageWidgetId ];
      this.$modal.confirm("确认删除？").then(function() {
        return deletePageWidget(pageWidgetIds);
      }).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadPageWidgetList();
        }
      }).catch(() => {});
    },
    handleEdit(row) {
      this.$router.push({ path: row.route, query: { id: row.pageWidgetId } });
    },
    handlePublish(row) {
      const pageWidgetIds = [ row.pageWidgetId ];
      publishPageWidgets(pageWidgetIds).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess(response.msg);
          this.loadPageWidgetList();
        }
      });
    },
    handlePreview(row) {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "pagewidget", dataId: row.pageWidgetId },
      });
      window.open(routeData.href, '_blank');
    }
  }
};
</script>
<style>
.pagewidget-container .el-card {
  margin-bottom: 10px;
}
.pagewidget-container .el-card__body {
  padding: 0;
}
.pagewidget-container .item-state {
  text-align: left;
}
.pagewidget-container .item-toolbar {
  text-align: right;
}
.pagewidget-container .item-catalog {
  margin-right: 5px;
}
.pagewidget-container .item-row1, .item-row2 {
  padding: 10px;
}
.pagewidget-container .item-row1 .item-name {
  font-size: 16px;
  font-weight: 400;
}
.pagewidget-container .item-row2 {
  color: #999;
}
.pagewidget-container .item-row3 {
  background-color: #f9f9f9;
  padding: 10px;
}
</style>