<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button plain
                      type="primary"
                      icon="el-icon-plus"
                      size="mini"
                      @click="handleAdd">新建</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form :model="queryParams"
                ref="queryForm"
                :inline="true"
                size="mini"
                class="el-form-search">
          <el-form-item prop="siteName">
            <el-input placeholder="站点名称" v-model="queryParams.siteName"></el-input>
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
      </el-col>
    </el-row>
    <el-row>
      <el-table v-loading="siteListLoading"
            :data="siteList"
            style="width:100%;line-height: normal;">
        <el-table-column label="ID"
                          width="200"
                          prop="siteId" />
        <el-table-column label="站点名称">
          <template slot-scope="scope">
            <el-link type="primary"
                      @click="handleEdit(scope.row)"
                      class="link-type">
              <span>{{ scope.row.name }}</span>
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="目录名"
                          width="200"
                          prop="path" />
        <el-table-column label="操作"
                          align="center"
                          width="300" 
                          class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button size="mini"
                        type="text"
                        icon="el-icon-s-promotion"
                        @click="handlePublish(scope.row)">发布首页</el-button>
            <el-button size="mini"
                        type="text"
                        @click="handlePreview(scope.row)"><svg-icon icon-class="eye-open" class="mr1"></svg-icon>预览</el-button>
            <el-button size="mini"
                        type="text"
                        icon="el-icon-edit"
                        @click="handleEdit(scope.row)">修改</el-button>
            <el-button size="mini"
                        type="text"
                        icon="el-icon-delete"
                        @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="siteTotal>0"
                :total="siteTotal"
                :page.sync="queryParams.pageNum"
                :limit.sync="queryParams.pageSize"
                @pagination="loadSiteList" />
    </el-row>
    <!-- 添加站点对话框 -->
    <el-dialog title="新建站点"
               :visible.sync="open"
               width="600px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="80px"
               class="el-form-dialog">
        <el-form-item label="名称"
                      prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="目录"
                      prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="资源域名"
                      prop="resourceUrl">
          <el-input v-model="form.resourceUrl" />
        </el-form-item>
        <el-form-item label="描述"
                      prop="description">
          <el-input v-model="form.description"
                    type="textarea"
                    maxlength="100" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary"
                   @click="handleAddSave">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress title="删除站点" :open.sync="openProgress" :taskId="taskId" @close="handleCloseProgress"></cms-progress>
  </div>
</template>
<style scoped>
</style>
<script>
import { delSite, addSite, listSite, publishSite  } from "@/api/contentcore/site";
import CMSProgress from '@/views/components/Progress';

export default {
  name: "Site",
  components: {
    'cms-progress': CMSProgress,
  },
  data () {
    return {
      open: false,
      siteList: undefined,
      siteListLoading: true,
      siteTotal: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        siteName: undefined
      },
      // 表单参数
      form: {
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        path: [
          { required: true, message: "目录不能为空", trigger: "blur" }
        ]
      },
      openProgress: false,
      taskId: ""
    };
  },
  created () {
    this.loadSiteList();
  },
  methods: {
    /** 查询站点列表 */
    loadSiteList () {
      this.siteListLoading = true;
      listSite(this.queryParams).then(response => {
        this.siteList = response.data.rows;
        this.siteTotal = parseInt(response.data.total);
        this.siteListLoading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadSiteList();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 取消按钮
    cancel () {
      this.open = false;
      this.resetAddForm();
    },
    // 表单重置
    resetAddForm () {
      this.form = {
        name: undefined,
        description: undefined,
        path: undefined,
        url: undefined
      };
      this.resetForm("form");
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.resetAddForm();
      this.title = "添加站点";
      this.open = true;
    },
    /** 新增站点提交 */
    handleAddSave: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.parentId = 0;
          addSite(this.form).then(response => {
            this.$modal.msgSuccess("新增成功");
            this.open = false;
            if (this.siteList.length == 0) {
                this.$router.go(0); // 无站点时刷新下重置当前站点
            } else {
              this.loadSiteList();
            }
          });
        }
      });
    },
    handleEdit (row) {
      const siteId = row.siteId;
      this.$router.push({ path: "/cms/site/tabs", query: { siteId: siteId } });
    },
    /** 删除按钮操作 */
    handleDelete (row) {
      const siteId = row.siteId;
      this.$modal.confirm("是否确认删除？").then(function () {
        return delSite(siteId);
      }).then(response => {
        this.taskId = response.data;
        this.openProgress = true;
      }).catch(function () { });
    },
    handleCloseProgress() {
        this.$router.go(0); // 删除站点时刷新下，当前站点删除需要重置当前站点
    },
    handlePreview(row) {
      const siteId = row.siteId;
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "site", dataId: siteId },
      });
      window.open(routeData.href, '_blank');
    },
    handlePublish(row) {
      publishSite({ siteId: row.siteId, publishIndex: true }).then(response => {
        if (response.code == 200) {
          this.$modal.msgSuccess("发布成功");
        }
      });
    }
  }
};
</script>