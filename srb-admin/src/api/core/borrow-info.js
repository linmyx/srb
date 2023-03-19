import request from '@/utils/request'
export default {
  getList() {
    return request({
      url: `/admin/core/borrowerInfo/getBorrowerInfoList`,
      method: 'get'
    })
  },
  getShowInfo(id){
    return request({
        url: `/admin/core/borrowerInfo/getShowInfo/${id}`,
        method: 'get'
      })
  },
  approval(borrowInfoApprovalVO){
    return request({
        url: `/admin/core/borrowerInfo/approval`,
        method: 'post',
        data:borrowInfoApprovalVO
      })
  }
}