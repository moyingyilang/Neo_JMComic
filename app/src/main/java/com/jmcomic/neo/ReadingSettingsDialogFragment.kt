// ReadingSettingsDialogFragment.kt
class ReadingSettingsDialogFragment(
    private val currentMode: ReadingMode,
    private val onModeChanged: (ReadingMode) -> Unit
) : DialogFragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogReadingSettingsBinding.inflate(inflater, container, false)
        
        // 设置当前模式
        when (currentMode) {
            ReadingMode.HORIZONTAL -> binding.rbHorizontal.isChecked = true
            ReadingMode.VERTICAL -> binding.rbVertical.isChecked = true
        }
        
        // 阅读模式选择
        binding.rgReadingMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbHorizontal.id -> onModeChanged(ReadingMode.HORIZONTAL)
                binding.rbVertical.id -> onModeChanged(ReadingMode.VERTICAL)
            }
            dismiss()
        }
        
        // 亮度调节
        binding.sbBrightness.progress = getCurrentBrightness()
        binding.sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setScreenBrightness(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        return binding.root
    }
    
    private fun getCurrentBrightness(): Int {
        return try {
            (Settings.System.getInt(
                requireContext().contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) * 100 / 255)
        } catch (e: Exception) {
            50
        }
    }
    
    private fun setScreenBrightness(progress: Int) {
        val brightness = (progress * 255 / 100).coerceIn(0, 255)
        val layoutParams = requireActivity().window.attributes
        layoutParams.screenBrightness = brightness / 255f
        requireActivity().window.attributes = layoutParams
    }
    
    companion object {
        fun newInstance(currentMode: ReadingMode, onModeChanged: (ReadingMode) -> Unit): ReadingSettingsDialogFragment {
            return ReadingSettingsDialogFragment(currentMode, onModeChanged)
        }
    }
}