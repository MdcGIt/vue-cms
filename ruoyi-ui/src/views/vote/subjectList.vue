<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-if="subjectTableExpand"
          type="primary"
          plain
          icon="el-icon-minus"
          size="mini"
          @click="handleTableExpand"
        >收起</el-button>
        <el-button
          v-else
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleTableExpand"
        >展开</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAddVoteSubject"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleEditVoteSubject"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDeleteVoteSubject"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-close"
          size="mini"
          @click="handleClose"
        >{{ $t('Common.Close') }}</el-button>
      </el-col>
    </el-row>
    
    <el-table v-loading="loading" ref="subjectTable" :data="subjectList" @selection-change="handleSelectionChange">
      <el-table-column type="expand">
        <template slot-scope="scope">
          <el-table v-if="scope.row.type!=='input'"  :show-header="false" :data="scope.row.itemList">
            <el-table-column type="index" align="right" width="100">
              <template slot-scope="scope2">
                {{ subjectItemIndex(scope2.$index) }}
              </template>
            </el-table-column>
            <el-table-column width="120">
              <template slot-scope="scope2">
                <el-select v-model="scope2.row.type" size="mini" disabled>
                  <el-option
                    v-for="type in subjectItemTypes"
                    :key="type.id"
                    :label="type.name"
                    :value="type.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column>
              <template slot-scope="scope2">
                {{ scope2.row.content }}
              </template>
            </el-table-column>
            <el-table-column lable="票数" width="200">
              <template slot-scope="scope2">
                票数：{{ scope2.row.total }}
                <el-progress :percentage="itemProgressPercent(scope2.row)"></el-progress> 
              </template>
            </el-table-column>
          </el-table>
          <div v-else style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;">
            <i class="el-icon-info mr5"></i>输入类型主题无需配置选项
          </div>
        </template>
      </el-table-column>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        type="index"
        label="序号"
        width="50">
      </el-table-column>
      <el-table-column
        label="主题">
        <template slot-scope="scope">
          <el-link v-if="scope.row.type!='input'" type="primary" @click="handleItemList(scope.row)">{{ scope.row.title }}</el-link>
          <span v-else>{{ scope.row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" prop="type" width="280">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.VoteSubjectType" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="300"
        align="right">
        <template slot-scope="scope">
          <el-button
            plain
            size="mini"
            icon="el-icon-plus"
            @click="handleAddVoteSubject(scope.row)">插入主题</el-button>
          <el-button
            plain
            size="mini"
            icon="el-icon-edit"
            @click="handleEditVoteSubject(scope.row)">编辑</el-button>
          <el-button
            plain
            size="mini"
            icon="el-icon-delete"
            type="danger"
            @click="handleDeleteVoteSubject(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" style="width:400px" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio v-for="dict in dict.type.VoteSubjectType"
                              :key="dict.value"
                              :label="dict.value">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Confirm') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
    <!-- 选项列表 -->
    <el-drawer
      direction="rtl"
      size="60%"
      :with-header="false"
      :visible.sync="openSubjectItems"
      :before-close="handleSubjectItemsClose">
      <el-row :gutter="10" style="padding:10px">
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="el-icon-edit"
            size="mini"
            @click="handleSaveItems"
          >保存</el-button>
        </el-col>
      </el-row>
      <el-table v-loading="loadingItems" :data="itemList">
        <el-table-column align="center" width="50">
          <template slot-scope="scope">
            <el-button
              circle
              plain
              v-if="scope.$index>0"
              type="success"
              icon="el-icon-top"
              size="mini"
              @click="handleUpItem(scope.$index)"></el-button>
          </template>
        </el-table-column>
        <el-table-column align="center" width="50">
          <template slot-scope="scope">
            <el-button
              circle
              plain
              v-if="scope.$index<itemList.length-1"
              type="warning"
              icon="el-icon-bottom"
              size="mini"
              @click="handleDownItem(scope.$index)"></el-button>
          </template>
        </el-table-column>
        <el-table-column label="序号" type="index" align="center" width="80">
          <template slot-scope="scope">
            {{ subjectItemIndex(scope.$index) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template slot-scope="scope">
            <el-select v-model="scope.row.type">
              <el-option
                v-for="type in subjectItemTypes"
                :key="type.id"
                :label="type.name"
                :value="type.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="详情">
          <template slot-scope="scope">
            <el-input v-if="scope.row.type==='Text'" type="text" v-model="scope.row.content"></el-input>
          </template>
        </el-table-column>
        <el-table-column width="200">
          <template slot-scope="scope">
            <el-button
              plain
              type="primary"
              icon="el-icon-plus"
              size="mini"
              @click="handleAddItem(scope.$index)">插入</el-button>
            <el-button
              plain
              type="danger"
              icon="el-icon-delete"
              size="mini"
              @click="handleDeleteItem(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-row :gutter="10" style="padding:10px">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="el-icon-plus"
            size="mini"
            style="float:right"
            @click="handleAddItem(itemList.length)"
          >添加选项</el-button>
        </el-col>
      </el-row>
    </el-drawer>
  </div>
</template>

<script>
import { getVoteItemTypes } from "@/api/vote/vote";
import { getVoteSubjectList, getVoteSubjectDetail, addVoteSubject, updateVoteSubject, deleteVoteSubjects, getSubjectItems, saveSubjectItems } from "@/api/vote/subject";

export default {
  name: "VoteSubjectList",
  dicts: [ 'VoteSubjectType' ],
  data() {
    return {
      voteId: this.$route.query.voteId,
      loading: true,
      loadingItems: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      title: "",
      open: false,
      itemIndexName: [ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' ],
      subjectItemTypes: [],
      subjectList: [],
      subjectTableExpand: false,
      form: {},
      rules: {
        title: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
        type: [
          { required: true, message: "不能为空", trigger: "blur" }
        ],
      },
      openSubjectItems: false,
      currentSubjectId: undefined,
      itemList: []
    };
  },
  created() {
    this.loadVoteSubjectItemTypes();
    this.loadVoteSubjectList();
  },
  methods: {
    itemProgressPercent(row) {
      return row.voteTotal > 0 ? parseInt(row.total*100/row.voteTotal) : 0;
    },
    subjectItemIndex(index) {
      let num = parseInt(index / 26);
      index = index % 26;
      let name = this.itemIndexName[index];
      if (num > 0) {
        name = name + num;
      }
      return name;
    },
    loadVoteSubjectItemTypes() {
      getVoteItemTypes().then(response => {
        this.subjectItemTypes = response.data;
      });
    },
    loadVoteSubjectList() {
      this.loading = true;
      getVoteSubjectList(this.voteId).then(response => {
          this.subjectList = response.data.rows;
          this.loading = false;
        }
      );
    },
    loadVoteSubjectItemList() {
      this.loadingItems = true;
      getSubjectItems(this.currentSubjectId).then(response => {
          this.itemList = response.data.rows;
          this.loadingItems = false;
        }
      );
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      if (this.$refs.menu != undefined) {
        this.$refs.menu.setCheckedKeys([]);
      }
      this.form = {
        title: undefined,
        status: undefined,
        remark: undefined
      };
      this.resetForm("form");
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.subjectId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    handleAddVoteSubject(row) {
      this.reset();
      this.form = { 
        type: 'radio',
        nextSubjectId: row.subjectId || undefined
      }
      this.open = true;
      this.title = "新增主题信息";
    },
    handleEditVoteSubject(row) {
      this.reset();
      const subjectId = row.subjectId || this.ids[0];
      getVoteSubjectDetail(subjectId).then(response => {
        this.form = response.data;
        this.title = "编辑主题信息";
        this.open = true;
      });
    },
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.subjectId != undefined) {
            updateVoteSubject(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.Success'));
              this.open = false;
              this.loadVoteSubjectList();
            });
          } else {
            this.form.voteId = this.voteId;
            addVoteSubject(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
              this.open = false;
              this.loadVoteSubjectList();
            });
          }
        }
      });
    },
    handleDeleteVoteSubject(row) {
      const subjectIds = row.subjectId ? [ row.subjectId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteVoteSubjects(subjectIds);
      }).then(() => {
        this.loadVoteSubjectList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(() => {});
    },
    handleItemList(row) {
      this.currentSubjectId = row.subjectId;
      this.loadVoteSubjectItemList();
      this.openSubjectItems = true;
    },
    handleSubjectItemsClose() {
      this.currentSubjectId = undefined;
      this.openSubjectItems = false;
      this.loadVoteSubjectList();
    },
    handleAddItem(rowIndex) {
      this.itemList.splice(rowIndex, 0, { type: "Text" });
    },
    handleDeleteItem(rowIndex) {
      this.itemList.splice(rowIndex, 1);
    },
    handleUpItem(rowIndex) {
      this.itemList.splice(rowIndex,1,...this.itemList.splice(rowIndex - 1, 1 , this.itemList[rowIndex]));
    },
    handleDownItem(rowIndex) {
      this.itemList.splice(rowIndex + 1,1,...this.itemList.splice(rowIndex, 1 , this.itemList[rowIndex + 1]));
    },
    handleSaveItems() {
      const data = { subjectId: this.currentSubjectId, itemList: this.itemList }
      saveSubjectItems(data).then(response => {
        this.loadVoteSubjectItemList();
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
      });
    },
    handleClose() {
      const obj = { path: "/operations/vote" };
      this.$tab.closeOpenPage(obj);
    },
    handleTableExpand() {
      this.subjectList.forEach(row => this.$refs.subjectTable.toggleRowExpansion(row, !this.subjectTableExpand));
      this.subjectTableExpand = !this.subjectTableExpand;
    }
  }
};
</script>