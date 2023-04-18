<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'RuoYiVueCMSDoc',
  data() {
    return {
      url: 'https://gitee.com/liweiyi/RuoYi-Vue-CMS'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>