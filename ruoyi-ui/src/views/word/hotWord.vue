<template>
  <div class="tag-word-container">
    <el-row :gutter="24" v-loading="loading">
      <el-col :span="4" :xs="24">
        <cms-hotword-group-tree 
          ref="groupTree"
          :new-btn="true"    
          @node-click="handleTreeNodeClick">
        </cms-hotword-group-tree>
      </el-col>
      <el-col :span="20" :xs="24">
        <el-row :gutter="24" class="mb12">
          <el-col :span="12">
            <el-button 
              type="primary"
              icon="el-icon-plus"
              size="mini"
              plain
              :disabled="selectedGroupId==''"
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
            <el-button 
              type="danger"
              icon="el-icon-delete"
              size="mini"
              plain
              :disabled="selectedGroupId==''||selectedIds.length==0"
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
                <el-input v-model="queryParams.query" placeholder="输入热词查询">
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
          <el-table 
            v-loading="loading"
            :data="wordList"
            @selection-change="handleSelectionChange"
            @row-dblclick="handleEdit">
            <el-table-column type="selection"
                            width="50"
                            align="center" />
            <el-table-column type="index"
                            label="序号"
                            align="center"
                            width="50" />
            <el-table-column label="热词"
                            align="left"
                            prop="word" />
            <el-table-column label="链接"
                            align="left">
              <template slot-scope="scope">
                <el-link :href="scope.row.url" target="_blank">{{ scope.row.url }}</el-link>
              </template>
            </el-table-column>
            <el-table-column label="引用次数"
                            align="center"
                            prop="useCount" 
                            width="120"/>
            <el-table-column label="点击次数"
                            align="center"
                            prop="hitCount" 
                            width="120"/>
            <el-table-column label="添加时间"
                              align="center"
                              width="160">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="添加人"
                            align="center"
                            width="120"
                            prop="createBy" />
            <el-table-column :label="$t('Common.Operation')"
                            align="center"
                            width="180" 
                            class-name="small-padding fixed-width">
              <template slot-scope="scope">
                <el-button 
                  size="small"
                  type="text"
                  icon="el-icon-edit"
                  @click="handleEdit(scope.row)">{{ $t('Common.Edit') }}</el-button>
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
      </el-col>
    </el-row>
    <!-- 添加热词弹窗 -->
    <el-dialog title="添加热词"
               :visible.sync="open"
               :close-on-click-modal="false"
               width="500px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="80px">
        <el-form-item label="热词"
                      prop="word">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item label="链接"
                      prop="url">
          <el-input v-model="form.url" />
        </el-form-item>
        <el-form-item label="跳转方式"
                      prop="urlTarget">
          <el-radio-group v-model="form.urlTarget">
            <el-radio label="_self">原窗口</el-radio>
            <el-radio label="_blank">新窗口</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')"
                      prop="remark">
          <el-input v-model="form.remark" />
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
.el-form-search .el-form-item {
  margin-bottom: 0;
}
</style>
<script>
import { validURL } from '@/utils/validate'
import { getHotWordList, addHotWord, editHotWord, deleteHotWord } from "@/api/word/hotWord";
import CMSHotWordGroupTree from '@/views/word/hotWordGroupTree';
import CMSLogoView from '@/views/cms/components/LogoView';

export default {
  name: "CMSHotWord",
  components: {
    "cms-logo-view": CMSLogoView,
    'cms-hotword-group-tree': CMSHotWordGroupTree
  },
  data () {
    return {
      loading: false,
      selectedGroupId: '',
      selectedIds: '',
      wordList: [],
      total: 0,
      queryParams: {
        query: undefined,
        groupId: undefined,
        pageSize: 20,
        pageNo: 1
      },
      open: false,
      form: {},
      rules: {
        word: [
          { required: true, message: "热词不能为空", trigger: "blur" }
        ],
        url: [
          { required: true, message: "链接不能为空", trigger: "blur" },
          { 
            trigger: "blur", 
            validator: (rule, value, callback) => {
              if (!validURL(value)) {
                callback(new Error("请使用以http(s)://开头的标准网络地址"));
              } else {
                callback();
              }
            } 
          }
        ],
        urlTarget: [
          { required: true, message: "跳转方式不能为空", trigger: "blur" }
        ],
      }
    };
  },
  methods: {
    handleTabClick (tab, event) {
    },
    handleTreeNodeClick(data) {
      this.selectedGroupId = data && data != null ? data.id : '';
      if (this.selectedGroupId !== this.queryParams.groupId) {
        this.queryParams.groupId = this.selectedGroupId;
        if (this.selectedGroupId != '') {
          this.loadWordList();
        }
      }
    },
    loadWordList () {
      this.loading = true;
      getHotWordList(this.queryParams).then(response => {
        if (response.code == 200) {
          this.wordList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
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
    handleEdit (row) {
      this.reset();
      this.form = row;
      this.open = true;
    },
    submitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.groupId = this.selectedGroupId;
          if (this.form.wordId) {
            editHotWord(this.form).then(response => {
              if (response.code == 200) {
                this.$modal.msgSuccess(response.msg);
                this.open = false;
                this.loadWordList();
              }
            }); 
          } else {
            addHotWord(this.form).then(response => {
              if (response.code == 200) {
                this.$modal.msgSuccess(response.msg);
                this.open = false;
                this.loadWordList();
              }
            }); 
          }
        }
      });
    },
    handleDelete (row) {
      const wordIds = row.wordId ? [ row.wordId ] : this.selectedIds;
      this.$modal.confirm('是否确认删除选中的热词?').then(function() {
        return deleteHotWord(wordIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.loadWordList();
      }).catch(() => {});
    }
  }
};
</script>