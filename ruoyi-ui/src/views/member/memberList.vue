<template>
  <!-- 会员管理页 -->
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row>
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true" v-show="showSearch">
        <el-form-item prop="userName">
          <el-input
            v-model="queryParams.userName"
            clearable
            placeholder="用户名"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="nickName">
          <el-input
            v-model="queryParams.nickName"
            clearable
            placeholder="昵称"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="queryParams.email"
            clearable
            placeholder="Email"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="phoneNumber">
          <el-input
            v-model="queryParams.phoneNumber"
            clearable
            placeholder="手机号"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="status">
          <el-select 
            v-model="queryParams.status"
            clearable
            placeholder="状态"
            style="width: 110px">
            <el-option 
              v-for="dict in dict.type.MemberStatus"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value" />
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
        label="会员ID"
        align="center"
        :show-overflow-tooltip="true"
        width="140"
        prop="memberId" />
      <el-table-column 
        label="用户名"
        align="center"
        :show-overflow-tooltip="true"
        prop="userName" />
      <el-table-column 
        label="昵称"
        align="center"
        :show-overflow-tooltip="true"
        prop="nickName" />
      <el-table-column 
        label="邮箱"
        align="center"
        :show-overflow-tooltip="true"
        prop="email" />
      <el-table-column 
        label="手机号"
        align="center"
        :show-overflow-tooltip="true"
        prop="phoneNumber" />
      <el-table-column label="状态" align="center" prop="status" width="80">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.MemberStatus" :value="scope.row.status"/>
          </template>
        </el-table-column>
      <el-table-column 
        label="来源"
        align="center"
        width="100"
        :show-overflow-tooltip="true"
        prop="sourceType" />
      <el-table-column label="注册时间"
                       align="center"
                       prop="createTime"
                       width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最近登录"
                       align="center"
                       prop="createTime"
                       width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastLoginTime) }} - {{ scope.row.lastLoginIp }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')"
                       align="center"
                       width="180" 
                       class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini"
                     type="text"
                     icon="el-icon-key"
                     @click="handleResetPwd(scope.row)">重置密码</el-button>
          <el-button size="mini"
                     type="text"
                     icon="el-icon-edit"
                     @click="handleUpdate(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button size="mini"
                     type="text"
                     icon="el-icon-delete"
                     @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0"
                :total="total"
                :page.sync="queryParams.pageNum"
                :limit.sync="queryParams.pageSize"
                @pagination="getList" />


    <!-- 添加或修改用户配置对话框 -->
    <el-dialog 
      :title="title" 
      :visible.sync="open"
      :close-on-click-modal="false" 
      width="600px" 
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item v-if="form.memberId == undefined" label="用户名" prop="userName">
          <el-input v-model="form.userName" :maxlength="30" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="form.nickName" :maxlength="30" />
        </el-form-item>
        <el-form-item v-if="form.memberId == undefined" label="密码" prop="password">
          <el-input v-model="form.password" type="password" :maxlength="32" show-password/>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" :maxlength="30" />
        </el-form-item>
        <el-form-item label="手机号" prop="phoneNumber">
          <el-input v-model="form.phoneNumber" :maxlength="11" />
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker v-model="form.birthday" type="date"></el-date-picker>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.MemberStatus"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleSubmitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="handleCancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getMemberList, getMemberDetail, addMember, updateMember, deleteMembers, resetMemberPassword } from "@/api/member/member";

export default {
  name: "MemberList",
  dicts: [ 'MemberStatus' ],
  data () {
    const validateMember = (rule, value, callback) => {
        if (this.form.userName == '' && this.form.phoneNumber == '' && this.form.email == '') {
          callback(new Error("用户名/手机号/Email不能全为空"));
        } else {
          callback();
        }
      };
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
      // 表单参数
      form: {
        status: '0'
      },
      // 表单校验
      rules: {
        userName: [,
          {
            pattern: /^[A-Za-z][A-Za-z0-9_]+$/,
            message: "必须以字母开头，且只能为（大小写字母，数字，下滑线）",
            trigger: "blur"
          },
          { validator: validateMember }
        ],
        password: [
          { required: true, message: "密码不能为空", trigger: "blur" }
        ],
        email: [
          { validator: validateMember },
          {
            type: "email",
            message: "Email格式错误",
            trigger: "blur"
          }
        ],
        phoneNumber: [
          { validator: validateMember },
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: "手机号格式错误",
            trigger: "blur"
          }
        ],
        status: [
          { required: true, message: "状态不能为空", trigger: "blur" },
        ]
      }
    };
  },
  created () {
    this.getList();
  },
  methods: {
    getList () {
      this.loading = true;
      getMemberList(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
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
      this.ids = selection.map(item => item.memberId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    reset () {
      this.resetForm("form");
      this.form = { status: '0' };
    },
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = "添加会员信息";
    },
    handleUpdate (row) {
      this.reset();
      const memberId = row.memberId ? row.memberId : this.ids[0];
      getMemberDetail(memberId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改会员信息";
      });
    },
    handleCancel () {
      this.open = false;
      this.reset();
    },
    handleSubmitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.memberId != undefined) {
            updateMember(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMember(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete (row) {
      const memberIds = row.memberId ? [ row.memberId ] : this.ids;
      this.$modal.confirm("是否确认删除？").then(function () {
        return deleteMembers(memberIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(function () { });
    },
    handleResetPwd(row) {
      this.$prompt("请输入" + row.userName + "的新密码", this.$t('Common.Tips'), {
        confirmButtonText: this.$t('Common.Confirm'),
        cancelButtonText: this.$t('Common.Cancel'),
        closeOnClickModal: false
      }).then(({ value }) => {
          resetMemberPassword(row.memberId, value).then(response => {
            this.$modal.msgSuccess('修改成功，新密码是：' + value);
          });
        }).catch(() => {});
    },
  }
};
</script>