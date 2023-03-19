import request from '@/utils/request'
export default {
  getLendItemList(lendId) {
    return request({
      url: `/admin/core/lendItem/list/${lendId}`,
      method: 'get'
    })
  }
}