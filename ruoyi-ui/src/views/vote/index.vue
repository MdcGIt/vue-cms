<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="queryParams.title"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          clearable
          style="width: 100px"
        >
          <el-option
            v-for="dict in dict.type.VoteStatus"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['vote:add']"
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
          v-hasPermi="['vote:add', 'vote:edit']"
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
          v-hasPermi="['vote:delete']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="loadVoteList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="voteList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="voteId" width="140" />
      <el-table-column label="编码" prop="code" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="标题" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-link type="primary"
                    @click="handleVoteSubject(scope.row)"
                    class="link-type">
            <span>{{ scope.row.title }}</span>
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="时间" align="center" width="300">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }} - {{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.VoteStatus" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="参与数" align="center" prop="total" width="80" />
      <el-table-column label="用户类型" align="center" prop="userType" width="100" />
      <el-table-column label="结果类型" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.VoteViewType" :value="scope.row.viewType"/>
        </template>
      </el-table-column>
      <el-table-column label="日/总上限" align="center" width="140">
        <template slot-scope="scope">
          {{ scope.row.dayLimit + " / " + scope.row.totalLimit }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleVoteSubject(scope.row)"
            v-hasPermi="['vote:add', 'vote:edit']"
          >编辑主题</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['vote:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['vote:delete']"
          >{{ $t('Common.Delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadVoteList"
    />

    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" style="width:400px" />
        </el-form-item>
        <el-form-item prop="code">
          <span slot="label">
            <el-tooltip content="只能使用大小写字母、数字和下划线组合" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
            编码
          </span>
          <el-input v-model="form.code" style="width:400px" />
        </el-form-item>
        <el-form-item label="时间" prop="timeRange">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            value-format="yyyy-MM-dd HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="用户类型">
          <el-radio-group v-model="form.userType">
            <el-radio
              v-for="ut in userTypeOptions"
              :key="ut.id"
              :label="ut.id"
            >{{ ut.name }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日次数上限" prop="totalLimit">
          <el-input-number v-model="form.dayLimit" controls-position="right" :min="1" />
        </el-form-item>
        <el-form-item label="总次数上限" prop="totalLimit">
          <el-input-number v-model="form.totalLimit" controls-position="right" :min="1" />
        </el-form-item>
        <el-form-item label="结果查看方式" prop="viewType">
          <el-radio-group v-model="form.viewType">
            <el-radio
              v-for="dict in dict.type.VoteViewType"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.VoteStatus"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Confirm') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getVoteUserTypes, getVoteList, getVoteDetail, addVote, updateVote, deleteVotes } from "@/api/vote/vote";

export default {
  name: "VoteList",
  dicts: [ 'VoteStatus', 'VoteViewType' ],
  components: { 
  },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      voteList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        status: undefined
      },
      userTypeOptions: [],
      form: {},
      rules: {
        title: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]*$", message: "不能为空且只能使用字母、数字和下划线", trigger: "blur" }
        ],
        startTime: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        endTime: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        userType: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        dayLimit: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        totalLimit: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        resultViewType: [
          { required: true, message: "不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.loadVoteUserTypeOptions();
    this.loadVoteList();
  },
  methods: {
    loadVoteUserTypeOptions() {
      getVoteUserTypes().then(response => {
        this.userTypeOptions = response.data;
      })
    },
    loadVoteList() {
      this.loading = true;
      getVoteList(this.queryParams).then(response => {
          this.voteList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.voteList.forEach(vote => {
            vote.timeRange = [ vote.startTime, vote.endTime ]
          });
          this.loading = false;
        }
      );
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.loadVoteList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.voteId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset();
      this.form = { 
        status: '0',
        userType: 'ip',
        dayLimit: 1,
        totalLimit: 1
      }
      this.open = true;
      this.title = "新增调查投票";
    },
    handleUpdate(row) {
      this.reset();
      const voteId = row.voteId || this.ids[0];
      getVoteDetail(voteId).then(response => {
        this.form = response.data;
        this.form.timeRange = [ this.form.startTime, this.form.endTime ]
        this.open = true;
        this.title = "编辑调查投票";
      });
    },
    handleVoteSubject: function(row) {
      const voteId = row.voteId;
      this.$router.push({ path: "/operations/vote/subjects", query: { voteId: voteId } });
    },
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.startTime = this.form.timeRange[0];
          this.form.endTime = this.form.timeRange[1];
          if (this.form.voteId != undefined) {
            updateVote(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.Success'));
              this.open = false;
              this.loadVoteList();
            });
          } else {
            addVote(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
              this.open = false;
              this.loadVoteList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const voteIds = row.voteId ? [ row.voteId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteVotes(voteIds);
      }).then(() => {
        this.loadVoteList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(() => {});
    }
  }
};
</script>