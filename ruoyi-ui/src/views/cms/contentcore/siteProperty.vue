<template>
  <div class="">
    <el-row :gutter="24">
      <el-col :span="12">
        <el-row>
          <el-button 
            plain
            type="primary"
            icon="el-icon-plus"
            size="mini"
            @click="handleAdd">新增</el-button>
          <el-button 
            plain
            type="danger"
            icon="el-icon-delete"
            size="mini"
            :disabled="multiple"
            @click="handleDelete">删除</el-button>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input placeholder="名称/代码查询" v-model="queryParams.query"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="el-icon-search"
                @click="handleQuery">搜索</el-button>
              <el-button 
                icon="el-icon-refresh"
                @click="resetQuery">重置</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-table v-loading="loading"
              :data="propertyList"
              @selection-change="handleSelectionChange">
      <el-table-column type="selection"
                       width="55"
                       align="center" />
      <el-table-column label="ID"
                       align="center"
                       prop="propertyId"
                       width="180" />
      <el-table-column label="属性名称"
                       align="center"
                       prop="propName" />
      <el-table-column label="属性代码"
                       align="center"
                       prop="propCode" />
      <el-table-column label="属性值"
                       align="center"
                       prop="propValue" />
      <el-table-column label="备注"
                       align="center"
                       prop="remark" />
      <el-table-column label="操作"
                       align="center"
                       width="180" 
                       class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleEdit(scope.row)">修改</el-button>
          <el-button 
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0"
                :total="total"
                :page.sync="queryParams.pageNum"
                :limit.sync="queryParams.pageSize"
                @pagination="loadSitePropertyList" />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title"
               :visible.sync="open"
               :close-on-click-modal="false"
               width="500px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="80px">
        <el-form-item label="名称"
                      prop="propName">
          <el-input v-model="form.propName"/>
        </el-form-item>
        <el-form-item label="代码"
                      prop="propCode">
          <el-input v-model="form.propCode" />
        </el-form-item>
        <el-form-item label="属性值"
                      prop="propValue">
          <el-input v-model="form.propValue" />
        </el-form-item>
        <el-form-item label="备注"
                      prop="remark">
          <el-input v-model="form.remark"
                    type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { getSitePropertyList, addSiteProperty, saveSiteProperty, deleteSiteProperty  } from "@/api/contentcore/site";

export default {
  name: "CMSSitePropperty",
  props: {
    site: {
      type: String,
      default: undefined,
      required: false,
    }
  },
  data () {
    return {
      loading: false,
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      propertyList: [],
      title: "",
      open: false,
      queryParams: {
        siteId: this.site,
        pageNum: 1,
        pageSize: 20,
        query: undefined
      },
      form: {},
      rules: {
        propName: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        propCode: [
          { required: true, pattern: "^[A-Za-z0-9_]*$", message: "不能为空且只能使用字母、数字和下划线", trigger: "blur" }
        ]
      }
    };
  },
  watch: {
    siteId(newVal) {
      if (newVal != undefined && newVal != null && newVal.length > 0) {
        this.loadSitePropertyList();
      }
    },
  },
  created() {
    this.loadSitePropertyList();
  },
  methods: {
    loadSitePropertyList () {
      this.loading = true;
      getSitePropertyList(this.queryParams).then(response => {
        this.propertyList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadSitePropertyList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.queryParams.query = undefined;
      this.queryParams.pageNum = 1;
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.propertyId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    cancel () {
      this.open = false;
      this.resetForm("form");
    },
    handleAdd () {
      this.form = { siteId: this.queryParams.siteId };
      this.title = "添加属性";
      this.open = true;
    },
    handleEdit (row) {
      this.form = row;
      this.title = "编辑属性";
      this.open = true;
    },
    submitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.propertyId) {
            saveSiteProperty(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.open = false;
              this.loadSitePropertyList();
            }); 
          } else {
            this.form.siteId = this.queryParams.siteId;
            addSiteProperty(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.open = false;
              this.loadSitePropertyList();
            }); 
          }
        }
      });
    },
    handleDelete (row) {
      const propertyIds = row.propertyId ? [ row.propertyId ] : this.ids;
      this.$modal.confirm("确认删除？").then(function () {
        return deleteSiteProperty(propertyIds);
      }).then(() => {
        this.loadSitePropertyList();
        this.$modal.msgSuccess("删除成功");
      }).catch(function () { });
    }
  }
};
</script>