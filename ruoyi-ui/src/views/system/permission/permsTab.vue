<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane label="菜单权限" name="menu">
        <permission-menu 
          v-if="this.activeName=='menu'" 
          :ownerType="permsOwnerType"
          :owner="permsOwner">
        </permission-menu>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script>
import MenuPermission from './menuPerms';

export default {
  name: "PermisisonTab",
  components: {
    'permission-menu': MenuPermission,
  },
  props: {
    ownerType: {
      type: String,
      default: ""
    },
    owner: {
      type: String,
      default: ""
    }
  },
  watch: {
    ownerType(newV, oldV) {
        this.permsOwnerType = newV;
    },
    owner(newV, oldV) {
      this.permsOwner = newV;
    }
  },
  data () {
    return {
      activeName: 'menu',
      permsOwnerType: this.ownerType,
      permsOwner: this.owner
    };
  },
  methods: {
    handleTabClick (tab, event) {
    }
  }
};
</script>