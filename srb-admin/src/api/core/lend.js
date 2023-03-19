import request from '@/utils/request'
export default {
  getLend() {
    return request({
      url: `/admin/core/lend/getLendList`,
      method: 'get'
    })
  },
  getLendById(id) {
    return request({
      url: `/admin/core/lend/getLendInfoById/${id}`,
      method: 'get'
    })
  },
  makeLoan(id) {
    return request({
      url: `/admin/core/lend/makeLoan/${id}`,
      method: 'get'
    })
  }
}