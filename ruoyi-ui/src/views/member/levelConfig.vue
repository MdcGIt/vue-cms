<template>
  <!-- 会员等级配置页 -->
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row>
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true" v-show="showSearch">
        <el-form-item prop="levelType">
          <el-select
            v-model="queryParams.levelType"
            style="width: 160px"
            clearable
            placeholder="积分类型"
            @keyup.enter.native="handleQuery">
            <el-option
              v-for="lt in levelTypes"
              :key="lt.id"
              :label="lt.name"
              :value="lt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>
    <el-table v-loading="loading"
              :data="dataList"
              @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column 
        label="类型"
        align="center"
        prop="levelTypeName" />
      <el-table-column 
        label="等级"
        align="center">
        <template slot-scope="scope">
          Lv{{ scope.row.level }}
        </template>
      </el-table-column>
      <el-table-column 
        label="名称"
        align="center"
        prop="name" />
      <el-table-column 
        label="下一级所需经验值"
        align="center"
        prop="nextNeedExp" />
      <el-table-column 
        label="备注"
        align="center"
        prop="remark" />
      <el-table-column label="操作"
                       align="center"
                       width="180" 
                       class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini"
                     type="text"
                     icon="el-icon-edit"
                     @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini"
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
                @pagination="getList" />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title"
               :visible.sync="open"
               :close-on-click-modal="false"
               width="500px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="120px">
        <el-form-item label="积分类型" prop="levelType">
          <el-select
            v-model="form.levelType"
            :disabled="form.configId!=undefined&&form.configId!=0"
            style="width:100%">
            <el-option
              v-for="lt in levelTypes"
              :key="lt.id"
              :label="lt.name"
              :value="lt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-input-number v-model="form.level" :min="0" :disabled="form.configId!=undefined&&form.configId!=0" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="名称"
                      prop="name">
          <el-input v-model="form.name" style="width:100%"></el-input>
        </el-form-item>
        <el-form-item label="下一级所需经验"
                      prop="nextNeedExp">
          <el-input-number v-model="form.nextNeedExp" :min="0" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="备注"
                      prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="handleSubmitForm">确 定</el-button>
        <el-button @click="handleCancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getLevelTypes, getLevelConfigList, getLevelConfigDetail, addLevelConfig, updateLevelConfig, deleteLevelConfigs } from "@/api/member/levelConfig";

export default {
  name: "MemberExpOperation",
  data () {
    return {
      // 遮罩层
      loading: true,
      showSearch: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 发布通道表格数据
      dataList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      levelTypes: [],
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        levelType: [
          { required: true, message: "等级类型不能为空", trigger: "blur" }
        ],
        level: [
          { required: true, message: "等级不能为空", trigger: "blur" }
        ],
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" },
        ],
        nextNeedExp: [
          { required: true, message: "下一级所需经验不能为空", trigger: "blur" },
        ]
      }
    };
  },
  created () {
    this.getList();
    this.loadLevelTypes();
  },
  methods: {
    getList () {
      this.loading = true;
      getLevelConfigList(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    loadLevelTypes() {
      getLevelTypes().then(res => this.levelTypes = res.data)
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    reset () {
      this.resetForm("form");
      this.form = { exp : 0, dayLimit: 0, totalLimit: 0 }
    },
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = "添加操作项配置";
    },
    handleUpdate (row) {
      this.reset();
      const configId = row.configId ? row.configId : this.ids[0];
      getLevelConfigDetail(configId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改操作项配置";
      });
    },
    handleCancel () {
      this.open = false;
      this.reset();
    },
    handleSubmitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.configId != undefined) {
            updateLevelConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addLevelConfig(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete (row) {
      const configIds = row.configId ? [ row.configId ] : this.ids;
      this.$modal.confirm("是否确认删除？").then(function () {
        return deleteLevelConfigs(configIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(function () { });
    }
  }
};
</script>