public class AllSort {

    public static void quickSort(int[] a, int l, int r) {
        int mid = a[(l+r)/2], i = l, j = r;
        do {
            while (a[i] < mid) i++;
            while (a[j] > mid) j--;
            if (i <= j) {
                int t = a[i];
                a[i] = a[j];
                a[j] = t;
                i++;
                j--;
            }
        } while (i <= j);
        if (i < r) quickSort(a, i, r);
        if (j > l) quickSort(a, l, j);
    }

    public static void mergeSort(int[] a, int l, int r) {
        if (l >= r) return;
        int mid = (l+r)/2;
        mergeSort(a, l, mid);
        mergeSort(a, mid+1, r);
        int[] b = new int[r-l+1];
        int i = l, j = mid+1, k = 0;
        while (i <= mid && j <= r) {
            if (a[i] < a[j]) {
                b[k++] = a[i++];
            } else {
                b[k++] = a[j++];
            }
        }
        while (i <= mid) b[k++] = a[i++];
        while (j <= r) b[k++] = a[j++];
        for (i = 0; i < r-l+1; ++i) {
            a[i+l] = b[i];
        }
    }

    public static void selectSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n; ++i) {
            int minVal = a[i], minIndex = i;
            for (int j = i+1; j < n; ++j) {
                if (a[j] < minVal) {
                    minVal = a[j];
                    minIndex = j;
                }
            }
            int t = a[i];
            a[i] = minVal;
            a[minIndex] = t;
        }
    }

}