<template>
  <div class="app-container">
    <el-form ref="form" :model="form" label-width="220px">
      <el-form-item label="快捷导航菜单" prop="Shortcut">
        <treeselect
          v-model="form.Shortcut"
          :options="menuOptions"
          :multiple="true"
          :placeholder="$t('System.Menu.Placeholder.ParentMenu')"
        />
      </el-form-item>
      <el-form-item label="默认显示子标题" prop="ShowContentSubTitle">
        <el-switch
          v-model="form.ShowContentSubTitle"
          active-text="是"
          inactive-text="否"
          active-value="Y"
          inactive-value="N">
        </el-switch>
      </el-form-item>
      <el-form-item label="内容列表默认包含子栏目内容" prop="IncludeChildContent">
        <el-switch
          v-model="form.IncludeChildContent"
          active-text="是"
          inactive-text="否"
          active-value="Y"
          inactive-value="N">
        </el-switch>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="smjall" @click="handleSave">{{ $t('Common.Save') }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { getUserMenuTree } from "@/api/system/menu";
import { getUserPreferences, saveUserPreferences } from "@/api/system/user";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "Profile",
  components: { Treeselect },
  data() {
    return {
      menuOptions: [],
      form: {},
    };
  },
  created() {
    this.loadUserPreference();
    this.loadMenuTreeselect();
  },
  methods: {
    loadMenuTreeselect() {
      getUserMenuTree().then(response => {
        this.menuOptions = [{ id: '0', parentId: '', label: this.$t('APP.TITLE'), isRoot: true, isDefaultExpanded: true, children: response.data }];
      });
    },
    loadUserPreference() {
      getUserPreferences().then(response => {
        this.form = response.data;
      });
    },
    handleSave() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          saveUserPreferences(this.form).then(response => {
            this.$modal.msgSuccess(response.msg);
          });
        }
      });
    }
  }
};
</script>
