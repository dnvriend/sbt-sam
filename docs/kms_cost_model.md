# AWS KMS

## Cost model
Each customer master key (CMK) that you create in AWS Key Management Service, regardless of whether you use it with 
KMS-generated key material or key material imported by you, costs $1/month until you delete it.

AWS KMS retains and manages each previous version of the CMK to ensure you can decrypt older data. You **are not** charged 
for the following:

- CMKs that are scheduled for deletion. If you cancel the deletion during the waiting period, the CMK will incur charges 
as though it was never scheduled for deletion.
- Data keys, which are created by GenerateDataKey and GenerateDataKeyWithoutPlaintext API requests. You **are** charged for 
the **KMS API calls** whether you make these API requests directly or they are made on your behalf by an integrated AWS service. 
You **are not** charged an ongoing monthly fee for the data keys themselves as they are neither stored nor managed by AWS KMS.

You **are** charged for the following
- The free tier consists of: 20,000 requests/month
- Each API request of AWS Key Management Service (outside of the free tier) costs $0.03 per 10,000 requests

So for example, I have a file that I encrypt with 1 CMK, that is 1 dollar a month, and I encrypt 10.000 records,
then I do 2 million reads, that will cost me about 7 dollars per month.

```
1 CMK => 1 dollar a month
10,000 Encrypt requests (1 x 10,000 objects)
2,000,000 Decrypt requests to access the objects

Totals to:

$1.00  = 1 CMK
$5.97  = (1,990,000 requests (2,010,000 requests - 20,000 free tier requests) x $0.03 / 10,000 requests)
======
$6.97 dollar / month
```