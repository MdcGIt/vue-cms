<template>
  <div class="app-container">
    <el-row>
      <el-form :model="queryParams"
              ref="queryForm"
              :inline="true"
              class="el-form-search">
        <el-form-item prop="query">
          <el-input v-model="queryParams.query" size="small">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary"
                    icon="el-icon-search"
                    size="small"
                    @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh"
                    size="small"
                    @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-row>
    <el-row class="mb8">
      <el-button type="primary"
                  icon="el-icon-plus"
                  size="mini"
                  plain
                  @click="handleAdd">新增</el-button>
      <el-button type="danger"
                  icon="el-icon-delete"
                  size="mini"
                  plain
                  :disabled="selectedIds.length==0"
                  @click="handleDelete">删除</el-button>
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
      <el-table-column label="类型" align="center" prop="wordType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.SearchDictWordType" :value="scope.row.wordType"/>
        </template>
      </el-table-column>
        <el-table-column label="词语"
                        align="left"
                        prop="word" />
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
        <el-table-column label="操作"
                        align="center"
                        width="180" 
                        class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button 
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)">删除</el-button>
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
    <el-dialog title="添加词库新词"
               :visible.sync="open"
               :close-on-click-modal="false"
               width="500px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="40px">
        <el-form-item 
          label="类型"
          prop="wordType">
          <el-select v-model="form.wordType" clearable>
            <el-option
              v-for="dict in dict.type.SearchDictWordType"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item 
          label="词"
          prop="wordStr">
          <el-input type="textarea" :rows="5" v-model="form.wordStr" placeholder="每行一个" />
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
import { getDictWords, addDictWord, deleteDictWords } from "@/api/search/dictword";

export default {
  name: "SearchDictWord",
  dicts: ['SearchDictWordType'],
  data () {
    return {
      loading: false,
      selectedIds: [],
      wordList: [],
      total: 0,
      open: false,
      queryParams: {
        query: undefined,
        pageSize: 20,
        pageNo: 1
      },
      form: {},
      rules: {
        word: [
          { required: true, message: "新词不能为空", trigger: "blur" }
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
      getDictWords(this.queryParams).then(response => {
        this.wordList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
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
          this.form.words = this.form.wordStr.split("/n");
          this.form.wordStr = "";
          addDictWord(this.form).then(response => {
            this.$modal.msgSuccess(response.msg);
            this.open = false;
            this.resetQuery ();
          }); 
        }
      });
    },
    handleDelete (row) {
      const wordIds = row.wordId ? [ row.wordId ] : this.selectedIds;
      this.$modal.confirm('是否确认删除?').then(function() {
        return deleteDictWords(wordIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.resetQuery ();
      }).catch(() => {});
    }
  }
};
</script>