<template>
  <div class="app-container">
    <h4>借款信息</h4>
    <table
      class="table table-striped table-condenseda table-bordered"
      width="100%"
    >
      <tbody>
        <tr>
          <th width="15%">借款金额</th>
          <td width="35%">{{ borrowInfoDetail.borrowInfo.amount }}元</td>
          <th width="15%">借款期限</th>
          <td width="35%">{{ borrowInfoDetail.borrowInfo.period }}个月</td>
        </tr>
        <tr>
          <th>年化利率</th>
          <td>{{ borrowInfoDetail.borrowInfo.borrowYearRate * 100 }}%</td>
          <th>还款方式</th>
          <td>{{ borrowInfoDetail.borrowInfo.returnMethod }}</td>
        </tr>

        <tr>
          <th>资金用途</th>
          <td>{{ borrowInfoDetail.borrowInfo.moneyUse }}</td>
          <th>状态</th>
          <td>{{ borrowInfoDetail.borrowInfo.param.status }}</td>
        </tr>
        <tr>
          <th>创建时间</th>
          <td>{{ borrowInfoDetail.borrowInfo.createTime }}</td>
          <th></th>
          <td></td>
        </tr>
      </tbody>
    </table>

    <h4>借款人信息</h4>
    <table
      class="table table-striped table-condenseda table-bordered"
      width="100%"
    >
      <tbody>
        <tr>
          <th width="15%">借款人</th>
          <td width="35%">
            <b>{{ borrowInfoDetail.borrowDetail.name }}</b>
          </td>
          <th width="15%">手机</th>
          <td width="35%">{{ borrowInfoDetail.borrowDetail.mobile }}</td>
        </tr>
        <tr>
          <th>身份证</th>
          <td>{{ borrowInfoDetail.borrowDetail.idCard }}</td>
          <th>性别</th>
          <td>{{ borrowInfoDetail.borrowDetail.sex }}</td>
        </tr>
        <tr>
          <th>年龄</th>
          <td>{{ borrowInfoDetail.borrowDetail.age }}</td>
          <th>是否结婚</th>
          <td>{{ borrowInfoDetail.borrowDetail.marry }}</td>
        </tr>
        <tr>
          <th>学历</th>
          <td>{{ borrowInfoDetail.borrowDetail.education }}</td>
          <th>行业</th>
          <td>{{ borrowInfoDetail.borrowDetail.industry }}</td>
        </tr>
        <tr>
          <th>月收入</th>
          <td>{{ borrowInfoDetail.borrowDetail.income }}</td>
          <th>还款来源</th>
          <td>{{ borrowInfoDetail.borrowDetail.returnSource }}</td>
        </tr>
        <tr>
          <th>创建时间</th>
          <td>{{ borrowInfoDetail.borrowDetail.createTime }}</td>
          <th>状态</th>
          <td>{{ borrowInfoDetail.borrowDetail.status }}</td>
        </tr>
      </tbody>
    </table>

    <el-row style="text-align:center;margin-top: 40px;">
      <el-button @click="back">
        返回
      </el-button>
    </el-row>
  </div>
</template>

<script>
import borrowInfoApi from '@/api/core/borrow-info';
  export default {
    data() {
      return {
          borrowInfoDetail: {
            borrowInfo: {
              param: {}
            },
            borrowDetail: {}
        }
      }
    },
    created() {
       if(this.$route.params.id){
          this.getShowInfoId()
       }
    },
    methods: {
       getShowInfoId(){
        borrowInfoApi.getShowInfo(this.$route.params.id)
        .then(res=>{
          this.borrowInfoDetail=res.data
        })
       },
       back(){
        this.$router.push({ path: '/core/borrower/info-list' })
       }
    },
  }
</script>

<style>
.app-container{
  background: #fff;
  position: absolute;
  width:100%;
  height: 100%;
  overflow: auto;
}
table {
  border-spacing: 0;
  border-collapse: collapse;
}
.table-bordered {
  border: 1px solid #ddd;
}
.table-striped>tbody>tr:nth-child(odd)>td, .table-striped>tbody>tr:nth-child(odd)>th {
  background-color: #f9f9f9;
}
.table>thead>tr>th, .table>tbody>tr>th, .table>tfoot>tr>th,
.table>thead>tr>td, .table>tbody>tr>td, .table>tfoot>tr>td {
  font-size: 14px;
  color: #333;
  padding: 8px;
  line-height: 1.42857143;
  vertical-align: top;
  border-top: 1px solid #ddd;
}
.table>thead>tr>th, .table>tbody>tr>th, .table>tfoot>tr>th{
  text-align: right;
  width: 120px;
}
.table-bordered>thead>tr>th, .table-bordered>tbody>tr>th, .table-bordered>tfoot>tr>th, .table-bordered>thead>tr>td, .table-bordered>tbody>tr>td, .table-bordered>tfoot>tr>td {
  border: 1px solid #ddd;
}
.active_content{
  padding: 0 20px  ;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  background-color: #fff;
  overflow: hidden;
  color: #303133;
  transition: .3s;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);

}
.active_content h4{
  /*line-height: 0;*/
}
.active_content span{
  font-size: 12px;
  color: #999;
}

</style>