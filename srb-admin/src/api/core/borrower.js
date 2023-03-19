import request from '@/utils/request'
export default {
  getList(page,limit,keyword) {
    return request({
      url: `/admin/core/borrower/getBorrowerList/${page}/${limit}`,
      method: 'post',
      params:{keyword}
    })
  },
  show(id) {
    return request({
      url: `/admin/core/borrower/getBorrowerInfo/${id}`,
      method: 'get'
    })
  },
  approval(borrowerApproval) {
    return request({
      url: '/admin/core/borrower/approval',
      method: 'post',
      data: borrowerApproval
    })
  }
}