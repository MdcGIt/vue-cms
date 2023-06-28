<template>
  <div class="sensitive-word-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-button 
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
        <el-button 
          plain
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="selectedIds.length==0"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <el-col :span="12">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" placeholder="输入敏感词查询">
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    
    <el-row>
      <el-table v-loading="loading"
                :data="wordList"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection"
                        width="50"
                        align="center" />
        <el-table-column type="index"
                        label="序号"
                        align="center"
                        width="100" />
        <el-table-column label="敏感词"
                        align="left"
                        prop="word" />
        <el-table-column label="类型"
                        align="center"
                        width="100"
                        prop="type">
          <template slot-scope="scope">
            <el-tag :type="scope.row.type==='WHITE'?'success':''">{{ formatType(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="添加时间"
                          align="center"
                          width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="添加人"
                        align="center"
                        width="160"
                        prop="createBy" />
        <el-table-column :label="$t('Common.Operation')"
                        align="center"
                        width="180" 
                        class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button 
              size="small"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadWordList"
      />
    </el-row>
    <!-- 添加弹窗 -->
    <el-dialog title="添加敏感词"
               :visible.sync="open"
               :close-on-click-modal="false"
               width="500px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="80px">
        <el-form-item label="敏感词"
                      prop="word">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item label="类型"
                      prop="type">
          <el-radio-group v-model="form.type">
            <el-radio v-for="item in typeOptions" :key="item.id" :label="item.id">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { getSensitiveWordList, addSensitiveWord, deleteSensitiveWord } from "@/api/word/sensitiveWord";

export default {
  name: "CMSSensitiveWord",
  data () {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      selectedIds: [],
      // 资源表格数据
      wordList: [],
      total: 0,
      typeOptions: [
        { id: "BLACK", label: "敏感词" },
        { id: "WHITE", label: "白名单" }
      ],
      open: false,
      queryParams: {
        query: undefined,
        pageSize: 20,
        pageNo: 1
      },
      form: {},
      rules: {
        word: [
          { required: true, message: "敏感词不能为空", trigger: "blur" }
        ],
        type: [
          { required: true, message: "类型不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created () {
    this.loadWordList();
  },
  methods: {
    loadWordList () {
      this.loading = true;
      getSensitiveWordList(this.queryParams).then(response => {
        if (response.code == 200) {
          this.wordList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      });
    },
    formatType(type) {
      for(let i = 0; i < this.typeOptions.length; i++) {
        if (this.typeOptions[i].id === type) {
          return this.typeOptions[i].label;
        }
      }
      return "unknow";
    },
    cancel () {
      this.open = false;
      this.reset();
    },
    reset () {
      this.form = {};
    },
    handleQuery () {
      this.queryParams.pageNo = 1;
      this.loadWordList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.selectedIds = selection.map(item => item.wordId);
    },
    handleAdd () {
      this.reset();
      this.open = true;
    },
    submitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          addSensitiveWord(this.form).then(response => {
            if (response.code == 200) {
              this.$modal.msgSuccess(response.msg);
              this.open = false;
              this.loadWordList();
            }
          }); 
        }
      });
    },
    handleDelete (row) {
      const wordIds = row.wordId ? [ row.wordId ] : this.selectedIds;
      this.$modal.confirm('是否确认删除选中的敏感词?').then(function() {
        return deleteSensitiveWord(wordIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.loadWordList();
      }).catch(() => {});
    }
  }
};
</script>