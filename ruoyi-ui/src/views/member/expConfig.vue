<template>
  <!-- 会员等级经验值操作项配置页 -->
  <div class="app-container">
    <el-row>
      <el-col>
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="70px">
          <el-form-item label="积分类型" prop="levelType">
            <el-select
              v-model="queryParams.levelType"
              style="width: 160px"
              clearable
              @keyup.enter.native="handleQuery">
              <el-option
                v-for="lt in levelTypes"
                :key="lt.id"
                :label="lt.name"
                :value="lt.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="操作项ID" prop="opType">
            <el-input
              v-model="queryParams.opType"
              clearable
              style="width: 160px"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-row :gutter="10"
            class="mb8">
      <el-col :span="1.5">
        <el-button type="primary"
                   icon="el-icon-plus"
                   size="mini"
                   @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success"
                   icon="el-icon-edit"
                   size="mini"
                   :disabled="single"
                   @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger"
                   icon="el-icon-delete"
                   size="mini"
                   :disabled="multiple"
                   @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading"
              :data="dataList"
              @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column 
        label="操作项"
        align="center"
        width="180">
        <template slot-scope="scope">
          {{ scope.row.opTypeName }}[{{ scope.row.opType }}]
        </template>
      </el-table-column>
      <el-table-column 
        label="积分类型"
        align="center"
        prop="levelTypeName" />
      <el-table-column 
        label="经验值"
        align="center"
        width="100"
        prop="exp" />
      <el-table-column 
        label="日上限"
        align="center"
        width="100"
        prop="dayLimit">
        <template slot-scope="scope">
          <span v-if="scope.row.dayLimit==0">无限制</span>
          <span v-else>{{ scope.row.dayLimit }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        label="总上限"
        align="center"
        width="100"
        prop="totalLimit">
        <template slot-scope="scope">
          <span v-if="scope.row.totalLimit==0">无限制</span>
          <span v-else>{{ scope.row.totalLimit }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间"
                       align="center"
                       prop="createTime"
                       width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
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
               width="400px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="80px">
        <el-form-item label="操作项"
                      prop="opTypeName">
          <el-input v-model="form.opTypeName" :disabled="true" style="width: 186px" />
          <el-button 
            class="ml5" 
            icon="el-icon-search" 
            type="success" 
            :disabled="form.configId!=undefined&&form.configId!=0" 
            @click="handleOpenSelector()">{{ $t('Common.Select') }}</el-button>
        </el-form-item>
        <el-form-item label="积分类型"
                      prop="levelType">
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
        <el-form-item label="经验值"
                      prop="exp">
          <el-input-number v-model="form.exp" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="日上限"
                      prop="dayLimit">
          <el-input-number v-model="form.dayLimit" :min="0" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="总上限"
                      prop="totalLimit">
          <el-input-number v-model="form.totalLimit" :min="0" style="width:100%"></el-input-number>
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
    <!-- 选择操作项列表弹窗 -->
    <el-dialog :title="title"
               :visible.sync="selectorVisible"
               :close-on-click-modal="false"
               width="500px"
               append-to-body>
      <el-table 
        v-loading="loading"
        :height="500"
        :data="opTypes"
        highlight-current-row
        @row-dblclick="handleOpTypeDblClick"
        @current-change="handleOpTypeSelectionChange">
        <el-table-column type="index"
                        label="序号"
                        align="center"
                        width="50" />
        <el-table-column label="操作项"
                        align="left">
          <template slot-scope="scope">
            {{ scope.row.name }}[{{ scope.row.id }}]
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer"
            class="dialog-footer">
        <el-button type="primary"
                    :disabled="okBtnDisabled"
                    @click="handleSelectorOk">确 定</el-button>
        <el-button @click="handleSelectorCancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getLevelTypes } from "@/api/member/levelConfig";
import { getExpOperations, getExpConfigList, getExpConfigDetail, addExpConfig, updateExpConfig, deleteExpConfigs } from "@/api/member/expConfig";

export default {
  name: "MemberExpOperation",
  data () {
    return {
      // 遮罩层
      loading: true,
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
      opTypes: [],
      selectorVisible: false,
      okBtnDisabled: true,
      selectedOpType: undefined,
      selectedOpTypeName: undefined,
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        opType: [
          { required: true, message: "操作项不能为空", trigger: "blur" }
        ],
        levelType: [
          { required: true, message: "等级类型不能为空", trigger: "blur" }
        ],
        exp: [
          { required: true, message: "经验值不能为空", trigger: "blur" }
        ],
        dayLimit: [
          { required: true, message: "日次数不能为空", trigger: "blur" },
        ],
        totalLimit: [
          { required: true, message: "总次数不能为空", trigger: "blur" },
        ]
      }
    };
  },
  created () {
    this.getList();
    this.loadLevelTypes();
    this.loadExpOperations();
  },
  methods: {
    getList () {
      this.loading = true;
      getExpConfigList(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    loadExpOperations() {
      getExpOperations().then(res => this.opTypes = res.data); 
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
      getExpConfigDetail(configId).then(response => {
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
            updateExpConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addExpConfig(this.form).then(response => {
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
        return deleteExpConfigs(configIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(function () { });
    },
    handleOpenSelector() {
      this.selectorVisible = true;
    },
    handleSelectorOk() {
      if (this.selectedOpType) {
        this.form.opType = this.selectedOpType;
        this.form.opTypeName = this.selectedOpTypeName;
      }
      this.handleSelectorCancel();
    },
    handleSelectorCancel() {
      this.selectorVisible = false;
      this.selectedOpType = undefined;
    },
    handleOpTypeSelectionChange (selection) {
      if (selection) {
        this.selectedOpType = selection.id;
        this.selectedOpTypeName = selection.name
      }
    },
    handleOpTypeDblClick (row) {
      this.form.opType = row.id;
      this.form.opTypeName = row.name;
      this.handleSelectorCancel();
    }
  }
};
</script>