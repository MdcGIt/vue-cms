<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="配置ID" align="center" prop="configId" width="100" />
      <el-table-column label="密码长度" align="center" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.passwordLenMin }} - {{ scope.row.passwordLenMax }}</span>
        </template>
      </el-table-column>
      <el-table-column label="密码校验规则" align="center" prop="passwordRule" width="180">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.SecurityPasswordRule" :value="scope.row.passwordRule"/>
        </template>
      </el-table-column>
      <el-table-column label="密码过期时长" align="center" prop="passwordExipreSeconds" width="180" />
      <el-table-column label="密码重试阈值" align="center" prop="passwordRetryLimit" width="180" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:config:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:config:remove']"
          >{{ $t('Common.Delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

     <!-- 添加或修改参数配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" v-loading="loading" :rules="rules" label-width="200px">
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span>密码安全配置</span>
          </div>
          <el-form-item label="密码最小长度"
                        prop="passwordLenMin">
            <el-input-number v-model="form.passwordLenMin" controls-position="right" :min="6" :max="16"></el-input-number>
          </el-form-item>
          <el-form-item label="密码最大长度"
                        prop="passwordLenMax">
            <el-input-number v-model="form.passwordLenMax" controls-position="right" :min="16" :max="30"></el-input-number>
          </el-form-item>
          <el-form-item label="密码组成规则"
                        prop="passwordRule">
            <el-select v-model="form.passwordRule">
              <el-option
                v-for="item in dict.type.SecurityPasswordRule"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="密码中不能包含字符"
                        prop="passwordSensitive">
            <el-checkbox-group v-model="form.passwordSensitive">
                  <el-checkbox
                    v-for="item in dict.type.SecurityPasswordSensitive"
                    :key="item.value"
                    :label="item.value"
                  >{{item.label}}</el-checkbox>
                </el-checkbox-group>
          </el-form-item>
          <el-form-item label="弱密码"
                        prop="weakPasswords">
            <el-input type="textarea" v-model="form.weakPasswords" placeholder="每行一个弱密码" :rows="5"></el-input>
          </el-form-item>
          <el-form-item 
            label="密码过期时长（单位：秒）"
            prop="passwordExipreSeconds">
            <el-input-number v-model="form.passwordExipreSeconds" controls-position="right" :min="0" :max="8640000"></el-input-number>
            <i class="el-icon-info tips">0表示永不过期，最长不超过100天</i>
          </el-form-item>
          <el-form-item label="首次登录是否强制修改密码"
                        prop="forceModifyPwdAfterAdd">
            <el-switch
              v-model="form.forceModifyPwdAfterAdd"
              active-value="Y"
              inactive-value="N">
            </el-switch>
            <i class="el-icon-info tips">仅适用于后台添加用户</i>
          </el-form-item>
          <el-form-item label="重置密码后是否强制修改密码"
                        prop="forceModifyPwdAfterReset">
            <el-switch
              v-model="form.forceModifyPwdAfterReset"
              active-value="Y"
              inactive-value="N">
            </el-switch>
            <i class="el-icon-info tips">仅适用于后台重置用户密码</i>
          </el-form-item>
        </el-card>
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span>登录校验配置</span>
          </div>
          <el-form-item label="每日密码错误次数阈值"
                        prop="passwordRetryLimit">
            <el-input-number v-model="form.passwordRetryLimit" controls-position="right" :min="0"></el-input-number>
            <i class="el-icon-info tips">0表示无限制</i>
          </el-form-item>
          <el-form-item 
            label="密码错误阈值处理策略"
            prop="passwordRetryStrategy">
            <el-select v-model="form.passwordRetryStrategy" :disabled="form.passwordRetryLimit===0">
              <el-option
                v-for="item in dict.type.SecurityPasswordRetryStrategy"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item 
            v-if="form.passwordRetryStrategy==='2'"
            label="锁定用户时长（单位：秒）"
            prop="passwordRetryLockSeconds">
            <el-input-number v-model="form.passwordRetryLockSeconds" controls-position="right" :min="0" :max="31536000"></el-input-number>
            <i class="el-icon-info tips">0表示永久锁定，最长不超过365天</i>
          </el-form-item>
        </el-card>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { listSecurityConfigs, getSecurityConfig, addSecurityConfig, saveSecurityConfig, deleteSecurityConfig, changeConfigStatus } from "@/api/system/security";

export default {
  name: "SysSecurityConfig",
  dicts: [ "EnableOrDisable", "SecurityPasswordRule", "SecurityPasswordSensitive", "SecurityPasswordRetryStrategy" ],
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
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 参数表格数据
      configList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      form: {},
      // 表单校验
      rules: {
        passwordLenMin: [
          { required: true, message: "密码最小长度不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList () {
      this.loading = true;
      listSecurityConfigs().then(response => {
        this.configList = response.data.rows;
        this.total = parseInt(response.data.total);
        // if (!this.form.passwordSensitive) {
        //   this.$set(this.form, 'passwordSensitive', []);
        // }
        this.loading = false;
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        status: '1',
        passwordLenMin: 0,
        passwordLenMax: 0,
        passwordRule: '0',
        passwordSensitive: [],
        passwordExipreSeconds: 0,
        forceModifyPwdAfterAdd: 'N',
        forceModifyPwdAfterReset: 'N',
        passwordRetryLimit: 0,
        passwordRetryStrategy: '0',
        passwordRetryLockSeconds: 0,
      };
      this.resetForm("form");
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加安全配置";
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      this.reset();
      const configId = row.configId || this.ids
      getSecurityConfig(configId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改安全配置";
      });
    },
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.configId != undefined) {
            saveSecurityConfig(this.form).then(res => {
              this.$modal.msgSuccess(this.$t("Common.Success"));
              this.open = false;
              this.getList();
            });
          } else {
            addSecurityConfig(this.form).then(res => {
              this.$modal.msgSuccess(this.$t("Common.AddSuccess"));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const configIds = row.configId ? [ row.configId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
          return deleteSecurityConfig(configIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess(this.$t("Common.DeleteSuccess"));
        }).catch(() => {});
    },
    handleStatusChange (row) {
      const configId = row.configId;
      changeConfigStatus(configId).then(res => {
        this.$modal.msgSuccess(this.$t("Common.Success"));
        this.getList();
      });
    }
  }
};
</script>
<style scoped>
.tips {
  margin-left: 10px;
  color: #909399;
}
.el-form-item {
  margin-bottom: 12px;
}
.el-card {
  margin-bottom: 10px;
}
.el-input, .el-input-number  {
  width: 217px;
}
</style>